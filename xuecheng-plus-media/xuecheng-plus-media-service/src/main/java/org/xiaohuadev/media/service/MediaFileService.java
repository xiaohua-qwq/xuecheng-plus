package org.xiaohuadev.media.service;


import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.base.model.RestResponse;
import org.xiaohuadev.media.model.dto.QueryMediaParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileResultDto;
import org.xiaohuadev.media.model.po.MediaFiles;

import java.util.List;

/**
 * 媒资文件管理业务类
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return 分页查询结果
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto);

    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件相关信息
     * @param localFilePath       文件本地路径
     * @return 上传文件返回Dto
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,
                                          String localFilePath);

    /**
     * 仅用作于本类中非代理方法代码块 调用事务控制方法使用的接口
     */
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto,
                                        String bucket, String objectName);

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return org.xiaohuadev.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return org.xiaohuadev.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     *
     * @param fileMd5            文件md5
     * @param chunk              分块序号
     * @param localChunkFilePath 分块文件本地路径
     * @return org.xiaohuadev.base.model.RestResponse
     */
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

    /**
     * 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,
                                    UploadFileParamsDto uploadFileParamsDto);


}
