package org.xiaohuadev.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.media.config.MinioConfig;
import org.xiaohuadev.media.mapper.MediaFilesMapper;
import org.xiaohuadev.media.model.dto.QueryMediaParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileResultDto;
import org.xiaohuadev.media.model.po.MediaFiles;
import org.xiaohuadev.media.service.MediaFileService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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

    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件相关信息
     * @param localFilePath       文件本地路径
     * @return 上传文件返回Dto
     */
    @Override
    @Transactional
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //将文件上传到MinIO
        String filename = uploadFileParamsDto.getFilename(); //获取到文件拓展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension); //通过拓展名获取到mimeType

        String defaultFolderPath = this.getDefaultFolderPath(); //根据当前日期获取文件应存入MinIo的路径
        String fileMd5 = getFileMd5(new File(localFilePath)); //获取文件Md5值作为文件名拼接
        String objectName = defaultFolderPath + fileMd5; //拼接路径+文件名(Md5)
        boolean result = addMediaFiles2MinIo(localFilePath, mimeType, bucket_mediaFiles, objectName);
        if(!result){
            XueChengPlusException.cast("上传文件失败");
        }

        //将文件信息保存到数据库
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediaFiles, objectName);
        if(mediaFiles==null)XueChengPlusException.cast("文件上传成功但保存文件信息失败");

        //准备返回对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles==null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            mediaFiles.setId(fileMd5); //文件id
            mediaFiles.setCompanyId(companyId); //机构id
            mediaFiles.setBucket(bucket); //桶
            mediaFiles.setFilePath(objectName); //文件路径(对象名)
            mediaFiles.setFileId(fileMd5); //设置文件名(文件MD5值)
            mediaFiles.setUrl("/"+bucket+"/"+objectName); //访问URL 桶名+文件名就可以拼成访问路径
            mediaFiles.setCreateDate(LocalDateTime.now()); //上传时间
            mediaFiles.setStatus("1"); //设置状态 默认为正常
            mediaFiles.setAuditStatus("002003"); //设置审核状态

            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert<=0){
                log.error("向数据库保存文件信息失败 bucket:{} objectName:{}",bucket,objectName);
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
}
