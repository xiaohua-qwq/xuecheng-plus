package org.xiaohuadev.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import org.xiaohuadev.content.feignclient.MediaServiceClient;

import java.io.File;
import java.io.FileNotFoundException;

@SpringBootTest
public class FeignUploadTest {
    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Test
    void testFeignApi() throws FileNotFoundException {
        File file = new File("C:\\Users\\PC\\Desktop\\test.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        String upload = mediaServiceClient.upload(multipartFile, "course/120.html");
        if (upload.equals("?")) {
            System.out.println("走了降级逻辑");
        }
    }
}
