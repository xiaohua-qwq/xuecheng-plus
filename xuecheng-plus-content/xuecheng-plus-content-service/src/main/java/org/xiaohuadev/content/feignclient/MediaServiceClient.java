package org.xiaohuadev.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 远程调用媒资服务接口
 */
@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class},
        fallbackFactory = MediaServiceClientFallbackFactory.class) //熔断后可以使用普通熔断实现方法或者熔断工厂
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("filedata") MultipartFile fileData,
                         @RequestParam(value = "objectName", required = false) String objectName);

}
