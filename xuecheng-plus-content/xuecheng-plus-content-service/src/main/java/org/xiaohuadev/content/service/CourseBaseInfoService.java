package org.xiaohuadev.content.service;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.xiaohuadev.base.exception.ValidationGroups;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.model.dto.AddCourseDto;
import org.xiaohuadev.content.model.dto.CourseBaseInfoDto;
import org.xiaohuadev.content.model.dto.EditCourseDto;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;

/**
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {

    /**
     * 课程信息查询接口
     *
     * @param pageParams      分页信息
     * @param courseParamsDto 查询条件
     * @return 分页数据返回类
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    /**
     * 新增课程
     *
     * @param companyId    机构id
     * @param addCourseDto 要添加的课程信息
     * @return 添加到数据库的课程信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息
     *
     * @param courseId 课程id
     * @return 课程详细信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 根据课程id修改课程
     *
     * @param companyId     机构id
     * @param editCourseDto 要修改的课程信息
     * @return 修改后的课程详细信息
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 根据课程id删除课程
     *
     * @param courseId 课程id
     */
    public void deleteCourse(Long courseId);
}
