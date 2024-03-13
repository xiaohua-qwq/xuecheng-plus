package org.xiaohuadev.content.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaohuadev.messagesdk.model.po.MqMessage;
import org.xiaohuadev.messagesdk.service.MessageProcessAbstract;
import org.xiaohuadev.messagesdk.service.MqMessageService;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        //分片索引
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        this.process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    /**
     * @param mqMessage 执行任务内容
     * @return boolean true:处理成功，false处理失败
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //将课程静态化资源上传到MinIO
        generateCourseHtml(mqMessage, courseId);

        //向elasticsearch写索引数据
        saveCourseIndex(mqMessage, courseId);

        //向Redis写缓存
        saveCourseCache(mqMessage, courseId);

        //返回true表示任务完成
        return true;
    }

    //生成课程静态化页面并上传至文件系统
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        //进行幂等性判断 取出该阶段的执行状态
        Long taskId = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("课程静态化已完成");
            return;
        }

        //TODO

        //处理完成 修改当前任务状态为完成
        mqMessageService.completedStageOne(taskId);
    }

    //保存课程索引信息
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        Long taskId = mqMessage.getId();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("索引已创建");
            return;
        }

        //TODO

        mqMessageService.completedStageTwo(taskId);
    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        Long taskId = mqMessage.getId();
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0) {
            log.debug("数据缓存已建立");
            return;
        }

        //TODO

        mqMessageService.completedStageThree(taskId);
    }


}
