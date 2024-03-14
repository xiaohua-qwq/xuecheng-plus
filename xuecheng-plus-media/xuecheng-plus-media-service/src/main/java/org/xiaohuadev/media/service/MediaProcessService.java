package org.xiaohuadev.media.service;

import org.xiaohuadev.media.model.po.MediaProcess;

import java.util.List;

/**
 * 分布式调用任务处理服务
 */
public interface MediaProcessService {
    /**
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * 获取待处理任务
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 根据任务id获取任务 获取成功开启任务 失败则丢弃此任务(有其他执行器执行了)
     * @param id 任务id
     * @return 是否获取到任务
     */
    public boolean startTask(Long id);

    /**
     * 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);


}
