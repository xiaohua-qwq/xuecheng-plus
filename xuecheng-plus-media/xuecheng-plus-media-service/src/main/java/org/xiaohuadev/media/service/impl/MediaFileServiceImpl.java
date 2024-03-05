package org.xiaohuadev.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.CommonError;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.base.model.RestResponse;
import org.xiaohuadev.media.config.MinioConfig;
import org.xiaohuadev.media.mapper.MediaFilesMapper;
import org.xiaohuadev.media.model.dto.QueryMediaParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileResultDto;
import org.xiaohuadev.media.model.po.MediaFiles;
import org.xiaohuadev.media.service.MediaFileService;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Autowired
    private MinioClient minioClient;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediaFiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    //注入自身 因为代理对象方法和事务控制注解同时存在时 实际上是执行对象本身方法的过程前后进行代理
    //而方法本身执行过程中调用带有@Transactional事务控制注解的方法 事务控制不生效
    //因此直接通过本类调用方法 因为本类在注入后本身就是一个代理对象 通过代理对象调用事务方法就不会产生冲突
    @Autowired
    private MediaFileService mediaFileService;

    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件相关信息
     * @param localFilePath       文件本地路径
     * @return 上传文件返回Dto
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,
                                          String localFilePath) {
        //将文件上传到MinIO
        String filename = uploadFileParamsDto.getFilename(); //获取到文件拓展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension); //通过拓展名获取到mimeType

        String defaultFolderPath = getDefaultFolderPath(); //根据当前日期获取文件应存入MinIo的路径
        String fileMd5 = getFileMd5(new File(localFilePath)); //获取文件Md5值作为文件名拼接
        String objectName = defaultFolderPath + fileMd5; //拼接路径+文件名(Md5)
        boolean result = addMediaFiles2MinIo(localFilePath, mimeType, bucket_mediaFiles, objectName);
        if (!result) {
            XueChengPlusException.cast("上传文件失败");
        }

        //将文件信息保存到数据库(非事务方法调同类一个事务方法) 用方法本身(是一个代理对象)调用
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb
                (companyId, fileMd5, uploadFileParamsDto, bucket_mediaFiles, objectName);
        if (mediaFiles == null) XueChengPlusException.cast("文件上传成功但保存文件信息失败");

        //准备返回对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 将文件信息添加到文件表
     *
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto,
                                        String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5); //文件id
            mediaFiles.setCompanyId(companyId); //机构id
            mediaFiles.setBucket(bucket); //桶
            mediaFiles.setFilePath(objectName); //文件路径(对象名)
            mediaFiles.setFileId(fileMd5); //设置文件名(文件MD5值)
            mediaFiles.setUrl("/" + bucket + "/" + objectName); //访问URL 桶名+文件名就可以拼成访问路径
            mediaFiles.setCreateDate(LocalDateTime.now()); //上传时间
            mediaFiles.setStatus("1"); //设置状态 默认为正常
            mediaFiles.setAuditStatus("002003"); //设置审核状态

            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("向数据库保存文件信息失败 bucket:{} objectName:{}", bucket, objectName);
                return null;
            }
            return mediaFiles;
        }
        return mediaFiles;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            log.error("获取文件Md5值时出错" + e.getMessage());
            return null;
        }
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }

    /**
     * 将文件上传到minIO
     *
     * @param localFilePath 文件路径
     * @param mimeType      媒体类型
     * @param bucket        桶
     * @param objectName    对象名
     * @return 是否成功
     */
    public boolean addMediaFiles2MinIo(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType) //传入文件格式
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("上传文件时错误 bucket:{} objectName{} 错误信息{}", bucket, objectName, e.getMessage());
            return false;
        }
    }

    /**
     * 上传分块
     *
     * @param fileMd5            文件md5
     * @param chunk              分块序号
     * @param localChunkFilePath 分块文件本地路径
     * @return org.xiaohuadev.base.model.RestResponse
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //获取到文件拓展名 由于分块文件没有拓展名 因此获取到的是通用MimeType字节流
        String mimeType = this.getMimeType(null);
        //获取到分块文件的路径
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5) + chunk;
        //上传文件到MinIO
        boolean result = this.addMediaFiles2MinIo(
                localChunkFilePath,
                mimeType,
                bucket_videoFiles,
                chunkFileFolderPath
        );
        //如果上传文件到MinIO返回的结果是false 则上传失败
        if (!result) return RestResponse.validfail(false, "上传分块文件失败");

        return RestResponse.success(true); //上传成功
    }

    //根据拓展名获取mimeType
    public String getMimeType(String extension) {
        if (extension == null) extension = "";
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; //通用MimeType字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return org.xiaohuadev.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //查询数据库该文件是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //文件在数据库中存在 查询Minio中文件是否存在
            GetObjectArgs objectArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(objectArgs);
                if (inputStream != null) return RestResponse.success(true); //文件已存在
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return org.xiaohuadev.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //分块路径为MD5值的前两位作为两个目录名 再加上chunk路径并拼接 即可得到分块文件的存储路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucket_videoFiles)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(objectArgs);
            if (inputStream != null) return RestResponse.success(true); //分块文件存在
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块文件不存在
        return RestResponse.success(false);
    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return 合并后的文件地址
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 合并分块
     *
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     */
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal,
                                    UploadFileParamsDto uploadFileParamsDto) {
        //合并MinIO中的文件
        List<ComposeSource> sourceList = new ArrayList<>();
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucket_videoFiles)
                    .object(chunkFileFolderPath + i)
                    .build();
            sourceList.add(composeSource);
        }
        String courseFileName = uploadFileParamsDto.getFilename();
        String fileExt = courseFileName.substring(courseFileName.lastIndexOf("."));
        String objectName = this.getFilePathByMd5(fileMd5, fileExt);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_videoFiles)
                .object(objectName)
                .sources(sourceList)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            log.error("MinIO合并文件{}时出错 桶{} 错误信息{}", objectName, bucket_videoFiles, e.getMessage());
            return RestResponse.validfail(false, "合并文件时出错");
        }

        //检查文件和源文件是否一致
        File file = this.downloadFileFromMinIO(bucket_videoFiles, objectName); //下载合并后的文件
        //计算合并后文件的MD5值
        try (FileInputStream stream = new FileInputStream(file)) {
            String mergeFileMd5 = DigestUtils.md5Hex(stream); //获取合并后文件的MD5值
            if (!fileMd5.equals(mergeFileMd5)) {
                log.error("校验文件合并后的MD5值时不一致 原始文件:{} 合并文件:{}", fileMd5, mergeFileMd5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            uploadFileParamsDto.setFileSize(file.length()); //设置文件的大小
        } catch (Exception e) {
            log.error("校验文件MD5值时出现异常");
            return RestResponse.validfail(false, "文件校验失败");
        }

        //同步到数据库(非事务方法调用事务方法 使用代理对象调用)
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5,
                uploadFileParamsDto, bucket_videoFiles, objectName);
        if (mediaFiles == null) return RestResponse.validfail(false, "文件入库失败");

        //清理分块文件
        this.cleanChunkFiles(chunkFileFolderPath, chunkTotal);
        return RestResponse.success(true);
    }

    private void cleanChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> deleteObjects = null;
        ArrayList<DeleteObject> list = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            DeleteObject deleteObject = new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i)));
            list.add(deleteObject);
        }
        deleteObjects = list;
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket_videoFiles)
                        .objects(deleteObjects)
                        .build()
        );
        //如果想要真正删除 需要遍历一下
        results.forEach(item -> {
            try {
                DeleteError deleteError = item.get();
            } catch (Exception e) {
                XueChengPlusException.cast("删除文件分块失败");
            }
        });
    }

    /**
     * 根据桶名称和文件名称从MinIO下载文件
     *
     * @param bucket     桶
     * @param objectName 文件名称
     * @return 下载后的文件File
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();
            InputStream stream = minioClient.getObject(getObjectArgs);
            minioFile = File.createTempFile("minio", ".temp");
            outputStream = new FileOutputStream(minioFile); //创建一个输出流 输入到临时文件中
            IOUtils.copy(stream, outputStream); //将从MinIO获取到的输入流拷贝到输出流中
            return minioFile; //返回拷贝后的文件
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return null;
    }
}
