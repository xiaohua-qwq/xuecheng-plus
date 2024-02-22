package org.xiaohuadev.content.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xiaohuadev.content.model.po.CourseCategory;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    /**
     * 子节点列表 列表中的子节点当前字段应为空
     */
    List<CourseCategoryTreeDto> childrenTreeNode;

}
