package org.xiaohuadev.media;

import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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

    //将分块上传到Minio
    @Test
    void uploadChunk() throws Exception {
        for (int i = 0; i < 16; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("C:\\Users\\PC\\Desktop\\chunk\\" + i) //本地文件路径
                    .object("chunk/" + i) //minio中的对象名
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块" + i + "成功");
        }
    }

    //测试Minio合并文件
    @Test
    void testMerge() throws Exception {
        List<ComposeSource> sourceList = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket("testbucket")
                    .object("chunk/" + i)
                    .build();
            sourceList.add(composeSource);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge.mp4") //合并后的文件名 存放在testbucket的根目录下
                .sources(sourceList)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }

    @Test
    void name() throws Exception {
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("video")
                .filename("C:\\Users\\PC\\Desktop\\1.mp4")
                .object("4/d/4dbb5c162a3bd28c252e6c179fdaf7cb/chunk/0")
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE) //传入文件格式
                .build();
        minioClient.uploadObject(uploadObjectArgs);
    }
}
