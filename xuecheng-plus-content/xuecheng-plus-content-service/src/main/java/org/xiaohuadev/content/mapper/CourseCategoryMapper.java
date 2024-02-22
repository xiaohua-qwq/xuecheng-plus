package org.xiaohuadev.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;
import org.xiaohuadev.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String rootId);

}
