package org.xiaohuadev.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaohuadev.content.model.po.CourseTeacher;
import org.xiaohuadev.content.service.CourseTeacherService;

import java.util.List;

@RestController
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程id查询教师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> selectTeacherInfoByCourseId(@PathVariable Long courseId) {
        return courseTeacherService.selectTeacherInfoByCourseId(courseId);
    }

    @ApiOperation("添加或修改教师信息")
    @PostMapping("/courseTeacher")
    public CourseTeacher addOrUpdateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.addCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除教师信息")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacherByCourseIdAndTeacherId(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteTeacherByCourseIdAndTeacherId(courseId, teacherId);
    }

}
