package org.xiaohuadev.content.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.model.dto.AddCourseDto;
import org.xiaohuadev.content.model.dto.CourseBaseInfoDto;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.service.CourseBaseInfoService;

@RestController
@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody AddCourseDto addCourseDto) {
        //获取到用户所属的机构id
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }
}
