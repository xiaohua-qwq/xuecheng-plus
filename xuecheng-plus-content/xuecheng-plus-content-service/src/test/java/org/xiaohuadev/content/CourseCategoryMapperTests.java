package org.xiaohuadev.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.mapper.CourseBaseMapper;
import org.xiaohuadev.content.mapper.CourseCategoryMapper;
import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;
import org.xiaohuadev.content.model.po.CourseBase;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTests {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Test
    void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
