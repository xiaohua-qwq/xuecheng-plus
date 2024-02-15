package org.xiaohuadev.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.mapper.CourseBaseMapper;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.service.CourseBaseInfoService;

import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 课程信息查询接口
     *
     * @param pageParams      分页信息
     * @param courseParamsDto 查询条件
     * @return 分页数据返回类
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //组装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),
                CourseBase::getName, courseParamsDto.getCourseName()); //课程名称

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus, courseParamsDto.getAuditStatus()); //课程审核状态

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()),
                CourseBase::getStatus, courseParamsDto.getPublishStatus()); //课程发布状态

        //组装分页条件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //从数据库进行分页查询
        Page<CourseBase> result = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = result.getRecords();
        long total = result.getTotal();

        return new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
    }
}
