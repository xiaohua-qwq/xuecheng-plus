package org.xiaohuadev.content.service;

import org.xiaohuadev.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * 课程发布相关接口
 */
public interface CoursePublishService {
    /**
     * 根据课程id返回课程信息预览数据模型
     *
     * @param courseId 课程id
     * @return 课程信息预览数据模型
     */
    public CoursePreviewDto preview(Long courseId);

    /**
     * 提交审核
     *
     * @param companyId 机构id
     * @param courseId  课程iid
     */
    public void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布接口
     *
     * @param companyId 机构id
     * @param courseId  课程id
     */
    public void publish(Long companyId, Long courseId);

    /**
     * 课程静态化
     *
     * @param courseId 课程id
     * @return File 静态化文件
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param file 静态化文件
     */
    public void uploadCourseHtml(Long courseId, File file);


}
