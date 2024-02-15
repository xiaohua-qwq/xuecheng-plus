package org.xiaohuadev.content.service;

import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    /**
     * 课程分类树形结构查询
     *
     * @return 根据根节点id查询到的子节点集合
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String rootId);

}
