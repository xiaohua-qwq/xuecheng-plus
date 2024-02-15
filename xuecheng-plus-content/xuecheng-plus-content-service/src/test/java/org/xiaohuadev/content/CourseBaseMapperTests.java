package org.xiaohuadev.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.mapper.CourseBaseMapper;
import org.xiaohuadev.content.model.po.CourseBase;

import java.util.List;

@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseBaseMapper() {
        //CourseBase courseBase = courseBaseMapper.selectById(18);
        //Assertions.assertNotNull(courseBase); //断言这个数据不为空

        //创建分页查询dto 用于构建查询条件
        QueryCourseParamsDto queryCourseParams = new QueryCourseParamsDto();
        queryCourseParams.setCourseName("java");

        //构建查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()),
                CourseBase::getName, queryCourseParams.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getAuditStatus()),
                CourseBase::getAuditStatus, queryCourseParams.getAuditStatus());

        //构建分页条件 参数(当前页码,每页记录数)
        Page<CourseBase> page = new Page<>(1, 2);

        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = pageResult.getRecords(); //数据列表
        long total = pageResult.getTotal(); //总记录数

        //模拟组装controller返回的PageResult
        PageResult<CourseBase> PageResult = new PageResult<>(records, total, 1, 2);
        System.out.println(PageResult);
    }
}
