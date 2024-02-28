package org.xiaohuadev.media;

import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.io.IOUtil;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 测试Minio的SDK
 */
public class MinioTest {
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://211.101.233.206:19000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    //添加文件
    @Test
    void test_uploadFile() {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket") //桶
                            .object("1.txt") //对象名
                            .filename("C:\\Users\\PC\\Desktop\\1.txt")
                            .build());
            System.out.println("upload file complete");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //删除文件
    @Test
    void test_delete() {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket("testbucket")
                    .object("1.txt")
                    .build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //查询文件(下载文件)
    @Test
    void test_getFile() {
        try {
            GetObjectArgs objectArgs = GetObjectArgs.builder()
                    .bucket("testbucket")
                    .object("1.txt")
                    .build();
            FilterInputStream inputStream = minioClient.getObject(objectArgs);
            FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\PC\\Desktop\\1.txt"));
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
