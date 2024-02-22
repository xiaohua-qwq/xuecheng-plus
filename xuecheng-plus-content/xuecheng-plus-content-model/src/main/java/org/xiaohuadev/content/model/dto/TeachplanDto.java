package org.xiaohuadev.content.model.dto;

import lombok.Data;
import org.xiaohuadev.content.model.po.Teachplan;
import org.xiaohuadev.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * 课程计划信息
 */
@Data
public class TeachplanDto extends Teachplan {
    //本章节下的小章节列表
    private List<TeachplanDto> teachPlanTreeNodes;

    //本章节关联的媒资信息
    private TeachplanMedia teachplanMedia;
}
