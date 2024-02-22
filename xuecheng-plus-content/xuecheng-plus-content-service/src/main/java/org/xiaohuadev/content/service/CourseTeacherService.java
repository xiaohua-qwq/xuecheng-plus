package org.xiaohuadev.content.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.xiaohuadev.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    /**
     * 根据课程id查询课程教师信息
     *
     * @param courseId 课程id
     * @return 教师信息集合
     */
    public List<CourseTeacher> selectTeacherInfoByCourseId(Long courseId);

    /**
     * 根据传入的课程id添加教师信息 或 根据传入的教师信息和课程id 修改教师信息
     *
     * @param courseTeacher 教师信息
     * @return 来自数据库的教师信息
     */
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 根据课程id和教师id删除教师信息
     *
     * @param courseId  课程id
     * @param teacherId 教师id(主键)
     */
    public void deleteTeacherByCourseIdAndTeacherId(Long courseId, Long teacherId);
}
