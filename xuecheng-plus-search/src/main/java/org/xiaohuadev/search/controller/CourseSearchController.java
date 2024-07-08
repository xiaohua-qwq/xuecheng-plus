package org.xiaohuadev.search.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.search.dto.SearchCourseParamDto;
import org.xiaohuadev.search.dto.SearchPageResultDto;
import org.xiaohuadev.search.po.CourseIndex;
import org.xiaohuadev.search.service.CourseSearchService;

/**
 * @description 课程搜索接口
 * @author Mr.M
 * @date 2022/9/24 22:31
 * @version 1.0
 */
@Api(value = "课程搜索接口",tags = "课程搜索接口")
 @RestController
 @RequestMapping("/course")
public class CourseSearchController {

 @Autowired
 CourseSearchService courseSearchService;


 @ApiOperation("课程搜索列表")
  @GetMapping("/list")
 public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto){

    return courseSearchService.queryCoursePubIndex(pageParams,searchCourseParamDto);
   
  }
}
