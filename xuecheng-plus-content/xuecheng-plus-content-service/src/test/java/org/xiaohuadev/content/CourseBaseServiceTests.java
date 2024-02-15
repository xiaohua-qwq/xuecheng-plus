package org.xiaohuadev.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.mapper.CourseBaseMapper;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.service.CourseBaseInfoService;

import java.util.List;

@SpringBootTest
public class CourseBaseServiceTests {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Test
    void testCourseBaseMapper() {

        QueryCourseParamsDto queryCourseParams = new QueryCourseParamsDto();
        queryCourseParams.setCourseName("java");
        queryCourseParams.setAuditStatus("202004");

        PageParams pageParams = new PageParams();
        pageParams.setPageNo(2L);
        pageParams.setPageSize(2L);

        PageResult<CourseBase> result = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
        System.out.println(result);
    }
}
