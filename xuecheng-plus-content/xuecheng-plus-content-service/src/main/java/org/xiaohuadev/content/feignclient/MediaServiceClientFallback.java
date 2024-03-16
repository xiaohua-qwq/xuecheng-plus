package org.xiaohuadev.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * 一般熔断后降级策略 无法获取到异常信息
 * 实现MediaServiceClient接口中的方法 在实现的方法内部定义降级后的逻辑
 */
public class MediaServiceClientFallback implements MediaServiceClient {
    @Override
    public String upload(MultipartFile fileData, String objectName) {
        return null;
    }
}
