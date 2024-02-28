package org.xiaohuadev.media.api;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.media.model.dto.QueryMediaParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileParamsDto;
import org.xiaohuadev.media.model.dto.UploadFileResultDto;
import org.xiaohuadev.media.model.po.MediaFiles;
import org.xiaohuadev.media.service.MediaFileService;

import java.awt.*;
import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    private MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

    }

    @ApiOperation("图片上传接口")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile fileData) {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileData.getOriginalFilename()); //设置原始文件名称
        uploadFileParamsDto.setFileSize(fileData.getSize()); //设置文件大小
        uploadFileParamsDto.setFileType("001001"); //设置文件类型为图片(对应数据字典)
        try {
            //此时图片已经上传到服务器 创建一个临时文件并将图片复制到临时文件中 获取文件路径并传给Service
            File tempFile = File.createTempFile("minio", "temp"); //创建临时文件(minio.temp)
            fileData.transferTo(tempFile); //将传入服务器的文件复制到临时文件中
            String absolutePath = tempFile.getAbsolutePath(); //获取文件绝对路径
            return mediaFileService.uploadFile(companyId, uploadFileParamsDto, absolutePath);
        } catch (Exception e) {
            XueChengPlusException.cast("控制器处理文件时错误");
            return null;
        }
    }

}
