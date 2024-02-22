package org.xiaohuadev.content.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xiaohuadev.base.exception.ValidationGroups;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.model.dto.AddCourseDto;
import org.xiaohuadev.content.model.dto.CourseBaseInfoDto;
import org.xiaohuadev.content.model.dto.EditCourseDto;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.service.CourseBaseInfoService;

@RestController
@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated
            (value = {ValidationGroups.Inster.class}) AddCourseDto addCourseDto) {
        //TODO 获取到用户所属的机构id
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据Id查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("根据Id修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated
            (value = {ValidationGroups.Update.class}) EditCourseDto editCourseDto) {
        //TODO 获取到用户所属的机构id
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("根据课程id删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        courseBaseInfoService.deleteCourse(courseId);
    }

}
