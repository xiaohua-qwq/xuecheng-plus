package org.xiaohuadev.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xiaohuadev.base.utils.Mp4VideoUtil;
import org.xiaohuadev.media.model.po.MediaProcess;
import org.xiaohuadev.media.service.MediaFileService;
import org.xiaohuadev.media.service.MediaProcessService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoTask {

    @Autowired
    private MediaProcessService mediaProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    //ffmpeg的路径
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 视频处理任务(分片广播)
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); //当前执行器的序号 从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); //执行器的总数

        //确定CPU核心数
        int CPUs = Runtime.getRuntime().availableProcessors();

        //从数据库中查询当前执行器要执行的任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList
                (shardIndex, shardTotal, CPUs);

        //获取实际任务数量
        int size = mediaProcessList.size();
        if (size <= 0) return;

        //创建一个线程池处理任务(不能超过CPU线程数)
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size); //创建一个计数器 大小为任务数量

        //遍历要执行的任务列表 开启线程
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(() -> {
                try {
                    //任务id
                    Long taskId = mediaProcess.getId();
                    //尝试获取任务
                    boolean flag = mediaProcessService.startTask(taskId);
                    if (!flag) return; //抢占任务失败

                    //执行转码
                    //从MinIO下载要转码的视频到本地
                    String bucket = mediaProcess.getBucket();
                    String filePath = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                    if (file == null) {
                        log.error("下载视频出错");
                        mediaProcessService.saveProcessFinishStatus
                                (taskId, "3", mediaProcess.getFileId(), null, "下载视频到本地失败");
                        return;
                    }
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = mediaProcess.getFileId() + ".mp4"; //文件的file_id就是MD5
                    //转换后mp4文件的路径
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件时异常:{}", e.getMessage());
                        mediaProcessService.saveProcessFinishStatus
                                (taskId, "3", mediaProcess.getFileId(), null, "创建临时文件时异常");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4_path);
                    //开始视频转换 成功将返回success 否则返回失败原因
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.error("视频转码失败 原因:{}", result);
                        mediaProcessService.saveProcessFinishStatus
                                (taskId, "3", mediaProcess.getFileId(), null, "视频转码失败");
                        return;
                    }
                    //上传转码后的视频
                    boolean isOk = mediaFileService.addMediaFiles2MinIo
                            (mp4File.getAbsolutePath(), "video/mp4", bucket, filePath);
                    if (!isOk) {
                        log.error("上传转码后的视频失败");
                        mediaProcessService.saveProcessFinishStatus
                                (taskId, "3", mediaProcess.getFileId(), null, "上传转码后的视频失败");
                        return;
                    }

                    String url = this.getFilePathByMd5(mediaProcess.getFileId(), ".mp4");

                    //将任务的处理结果保存到数据库
                    mediaProcessService.saveProcessFinishStatus
                            (taskId, "2", mediaProcess.getFileId(), url, null);

                } finally { //无论任务执行是否异常 都要让计数器减一 防止阻塞
                    countDownLatch.countDown(); //计数器减去一 代表当前任务已完成
                }
            });
        });
        //阻塞 等所有线程完成任务后 再继续执行
        countDownLatch.await(30, TimeUnit.MINUTES); //最多等待30分钟
    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return 合并后的文件地址
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
