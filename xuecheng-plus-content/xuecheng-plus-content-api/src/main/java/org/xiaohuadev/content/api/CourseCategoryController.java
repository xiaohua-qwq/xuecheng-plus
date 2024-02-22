package org.xiaohuadev.content.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;
import org.xiaohuadev.content.service.CourseCategoryService;

import java.util.List;

/**
 * 课程分类相关接口
 */
@RestController
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNode() {
        return courseCategoryService.queryTreeNodes("1");
    }
}
