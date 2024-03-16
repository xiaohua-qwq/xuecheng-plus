package org.xiaohuadev.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 降级工厂实现方法 实现FallbackFactory<Feign接口>
 * 好处是可以拿到熔断的异常信息
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) { //拿到了熔断时的异常信息
        return new MediaServiceClient() { //发生熔断后上游服务会调用此方法执行降级逻辑
            @Override
            public String upload(MultipartFile fileData, String objectName) {
                log.debug("远程调用上传接口发生熔断:{}", throwable.toString());
                return "?";
            }
        };
    }
}
