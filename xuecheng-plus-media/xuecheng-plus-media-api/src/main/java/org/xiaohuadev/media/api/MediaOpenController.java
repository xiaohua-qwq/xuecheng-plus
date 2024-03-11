package org.xiaohuadev.media.api;

import com.alibaba.nacos.common.utils.StringUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.base.model.RestResponse;
import org.xiaohuadev.media.model.po.MediaFiles;
import org.xiaohuadev.media.service.MediaFileService;

@RestController
@RequestMapping("/open")
public class MediaOpenController {
    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles != null && StringUtils.isNotBlank(mediaFiles.getUrl())) {
            String url = mediaFiles.getUrl();
            return RestResponse.success(url);
        }
        return RestResponse.validfail("视频正在准备中,请稍后再试");
    }

}
