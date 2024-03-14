package org.xiaohuadev.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 测试大文件上传(分块上传)
 */
public class BigFileTest {

    //测试分块
    /*@Test
    public void testChunk() throws Exception {
        //原文件路径
        File sourceFile = new File("C:\\Users\\PC\\Desktop\\1.mp4");
        //分块文件存储路径
        String chunkFilePath = "C:\\Users\\PC\\Desktop\\chunk\\";
        //每个分块的大小(每块5Mb)
        int chunkSize = 1024 * 1024 * 5;
        //计算分块个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        //读取流 用于读取原文件
        RandomAccessFile read_stream = new RandomAccessFile(sourceFile, "r");

        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i); //分块文件的路径和名称
            //创建一个读写流 用于写出分块文件
            RandomAccessFile readAndWrite_stream = new RandomAccessFile(chunkFile, "rw");
            int len = -1; //需要写入的数据
            while ((len = read_stream.read(bytes)) != -1) {
                readAndWrite_stream.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            readAndWrite_stream.close();
        }
        read_stream.close();
    }*/

    @Test
    public void testChunk() throws Exception {
        //源文件路径
        File sourceFile = new File("C:\\Users\\PC\\Desktop\\1.mp4");
        //分块文件夹路径
        String chunkFilePath = "C:\\Users\\PC\\Desktop\\chunk\\";
        //每个分块大小
        int chunkSize = 1024 * 1024 * 5;
        //计算要拆分成多少个分块
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        RandomAccessFile readSourceStream = new RandomAccessFile(sourceFile, "r");

        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            int len = -1;
            RandomAccessFile writeChunkFileStream = new RandomAccessFile(chunkFile, "rw");
            while ((len = readSourceStream.read(bytes)) != -1) {
                writeChunkFileStream.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            writeChunkFileStream.close();
        }
        readSourceStream.close();
    }

    //测试合并
    @Test
    public void testMerge() throws Exception {
        //块文件目录
        File chunkFiles = new File("C:\\Users\\PC\\Desktop\\chunk\\");
        //源文件
        File sourceFile = new File("C:\\Users\\PC\\Desktop\\1.mp4");
        //合并后的文件
        File mergeFile = new File("C:\\Users\\PC\\Desktop\\2.mp4");

        File[] files = chunkFiles.listFiles();
        List<File> list = Arrays.asList(files);

        //对文件集合进行排序
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        RandomAccessFile write = new RandomAccessFile(mergeFile, "rw");

        byte[] bytes = new byte[1024];
        for (File file : list) {
            RandomAccessFile read = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = read.read(bytes)) != -1) {
                write.write(bytes, 0, len);
            }
            read.close();
        }
        write.close();

        //两个文件MD5校验
        FileInputStream sourceStream = new FileInputStream(sourceFile);
        FileInputStream mergeStream = new FileInputStream(mergeFile);
        String source = DigestUtils.md5Hex(sourceStream);
        String merge = DigestUtils.md5Hex(mergeStream);
        if (source.equals(merge)) {
            System.out.println("合并文件成功");
        } else System.err.println("合并文件失败");
    }
}
