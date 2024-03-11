package org.xiaohuadev.media.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.media.mapper.MediaFilesMapper;
import org.xiaohuadev.media.mapper.MediaProcessHistoryMapper;
import org.xiaohuadev.media.mapper.MediaProcessMapper;
import org.xiaohuadev.media.model.po.MediaFiles;
import org.xiaohuadev.media.model.po.MediaProcess;
import org.xiaohuadev.media.model.po.MediaProcessHistory;
import org.xiaohuadev.media.service.MediaProcessService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    /**
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * 获取待处理任务
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    /**
     * 根据任务id获取任务 获取成功开启任务 失败则丢弃此任务(有其他执行器执行了)
     *
     * @param id 任务id
     * @return 是否获取到任务
     */
    @Override
    public boolean startTask(Long id) {
        int task = mediaProcessMapper.startTask(id);
        return task == 1;
    }

    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 保存任务结果
     *
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     */
    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //找到要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }

        //如果任务执行失败
        if (status.equals("3")) {
            mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1); //待处理任务表的失败次数要加一
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }


        //如果任务执行成功
        //将转码后的视频url更新到media_file表中
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        //修改待处理任务表中的任务信息
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        //将当前任务信息插入到history表中
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //删除待处理任务表中的当前任务记录
        mediaProcessMapper.deleteById(taskId);
    }
}
