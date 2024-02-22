package org.xiaohuadev.content.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.xiaohuadev.content.model.dto.SaveTeachplanDto;
import org.xiaohuadev.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程计划管理相关接口
 */
public interface TeachplanService {

    /**
     * 查询课程计划
     *
     * @param courseId 课程id
     * @return 课程计划结果集
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增章节/新增小章节/修改章节
     *
     * @param teachplan 要变更的章节信息
     */
    public void saveTeachplan(SaveTeachplanDto teachplan);

    /**
     * 根据课程id删除课程计划
     * @param courseId 课程id
     */
    public void deleteTeachplan(Long courseId);

    /**
     * 根据课程id 将课程在树中的排序向下移动
     * @param courseId 课程id
     */
    public void moveDown(Long courseId);

    /**
     * 根据课程id 将课程在树中的排序向上移动
     * @param courseId 课程id
     */
    public void moveUp(Long courseId);
}
