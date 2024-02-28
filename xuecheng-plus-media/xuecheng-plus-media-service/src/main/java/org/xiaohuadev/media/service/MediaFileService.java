package org.xiaohuadev.media.service;


import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
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
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return 分页查询结果
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     *
     * @param companyId 机构id
     * @param uploadFileParamsDto 文件相关信息
     * @param localFilePath 文件本地路径
     * @return 上传文件返回Dto
     */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,String localFilePath);
}
