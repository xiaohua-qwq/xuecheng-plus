package org.xiaohuadev.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.content.mapper.CourseTeacherMapper;
import org.xiaohuadev.content.model.po.CourseTeacher;
import org.xiaohuadev.content.service.CourseTeacherService;

import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * 根据课程id查询课程教师信息
     *
     * @param courseId 课程id
     * @return 教师信息集合
     */
    @Override
    public List<CourseTeacher> selectTeacherInfoByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    /**
     * 根据传入的课程id添加教师信息 或 根据传入的教师信息和课程id 修改教师信息
     *
     * @param courseTeacher 教师信息
     * @return 来自数据库的教师信息
     */
    @Override
    @Transactional
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher) {
        //校验参数合法性
        if (courseTeacher == null || courseTeacher.getCourseId() == null) {
            XueChengPlusException.cast("参数错误");
        }
        if (courseTeacher.getId() != null) {
            //是修改教师信息
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update <= 0) XueChengPlusException.cast("插入教师数据时错误");
            return courseTeacherMapper.selectById(courseTeacher.getId());
        }
        int insert = courseTeacherMapper.insert(courseTeacher);
        if (insert <= 0) XueChengPlusException.cast("插入教师数据时错误");
        return courseTeacher;
    }

    /**
     * 根据课程id和教师id删除教师信息
     *
     * @param courseId  课程id
     * @param teacherId 教师id(主键)
     */
    @Override
    public void deleteTeacherByCourseIdAndTeacherId(Long courseId, Long teacherId) {
        if (courseId == null || teacherId == null) XueChengPlusException.cast("参数错误");
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        int deleteLine = courseTeacherMapper.delete(queryWrapper);
        if (deleteLine <= 0) XueChengPlusException.cast("移除教师信息时错误");
    }
}
