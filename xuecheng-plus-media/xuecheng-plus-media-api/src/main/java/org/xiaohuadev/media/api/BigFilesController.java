package org.xiaohuadev.media.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xiaohuadev.base.model.RestResponse;
import org.xiaohuadev.media.model.dto.UploadFileParamsDto;
import org.xiaohuadev.media.service.MediaFileService;

import java.io.File;

@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    /**
     * 检查文件是否存在于数据库和MinIO
     *
     * @param fileMd5 文件MD5值 是数据库存储的主键
     * @return RestResponse<Boolean> 存在为true 不存在为false
     */
    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }


    /**
     * 检查文件分块是否存在于MinIO中
     *
     * @param fileMd5 文件MD5值
     * @param chunk   分块编号
     * @return RestResponse<Boolean> 存在为true 不存在为false
     */
    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk
    ) throws Exception {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk
    ) throws Exception {
        //将传入的文件复制到本地的临时文件 获取到临时文件的路径
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        //文件路径
        String localFilePath = tempFile.getAbsolutePath();

        return mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal
    ) throws Exception {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType("001002");
        return mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);

    }


}

