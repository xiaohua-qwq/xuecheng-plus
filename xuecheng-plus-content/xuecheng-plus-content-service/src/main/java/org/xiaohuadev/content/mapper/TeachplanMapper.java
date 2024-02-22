package org.xiaohuadev.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xiaohuadev.content.model.dto.TeachplanDto;
import org.xiaohuadev.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * 根据课程id查询课程计划
     *
     * @param courseId 课程id
     * @return 根据课程id查询出的结果集
     */
    public List<TeachplanDto> teachTreeNodes(Long courseId);

}
