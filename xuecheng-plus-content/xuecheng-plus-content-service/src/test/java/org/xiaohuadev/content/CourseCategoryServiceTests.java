package org.xiaohuadev.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.service.CourseBaseInfoService;
import org.xiaohuadev.content.service.CourseCategoryService;

import java.util.List;

@SpringBootTest
public class CourseCategoryServiceTests {
    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    void testCourseCategoryService() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
