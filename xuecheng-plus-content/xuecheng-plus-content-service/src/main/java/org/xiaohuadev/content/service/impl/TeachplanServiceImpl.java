package org.xiaohuadev.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.content.mapper.TeachplanMapper;
import org.xiaohuadev.content.model.dto.SaveTeachplanDto;
import org.xiaohuadev.content.model.dto.TeachplanDto;
import org.xiaohuadev.content.model.po.Teachplan;
import org.xiaohuadev.content.service.TeachplanService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * 查询课程计划
     *
     * @param courseId 课程id
     * @return 课程计划结果集
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.teachTreeNodes(courseId);
    }

    /**
     * 新增章节/新增小章节/修改章节
     *
     * @param teachplanDto 要变更的章节信息
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        if (teachplanDto.getId() == null) {
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplan);

            //设置排序字段
            //SQL语句为: select count(1) from teachplan where course_id = {课程id} and parentid = {父节点id}
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            Long courseId = teachplanDto.getCourseId();
            Long parentId = teachplanDto.getParentid();
            queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
            Integer count = teachplanMapper.selectCount(queryWrapper); //查询到同级别的课程数量
            teachplan.setOrderby(count + 1); //新增的课程排在后面 因此这个课程的排序值在原有的值上加1

            teachplanMapper.insert(teachplan);
        } else {
            //修改
            Teachplan teachplanFromDB = teachplanMapper.selectById(teachplanDto.getId());
            BeanUtils.copyProperties(teachplanDto, teachplanFromDB);
            teachplanMapper.updateById(teachplanFromDB);
        }
    }

    /**
     * 根据课程id删除课程计划
     *
     * @param courseId 课程id
     */
    @Override
    @Transactional
    public void deleteTeachplan(Long courseId) {
        Teachplan teachplan = teachplanMapper.selectById(courseId);
        if (teachplan.getParentid() == 0) { //是一个父章节 检查有没有子章节
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(Teachplan::getParentid, courseId);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count != 0) {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
            int deleteCount = teachplanMapper.deleteById(courseId);
            if (deleteCount <= 0) XueChengPlusException.cast("删除课程计划失败");
            return;
        }
        //是子章节 直接删除
        int deleteCount = teachplanMapper.deleteById(courseId);
        if (deleteCount <= 0) XueChengPlusException.cast("删除课程计划失败");
    }

    /**
     * 根据课程id 将课程在树中的排序向下移动
     *
     * @param id 课程id
     */
    @Override
    @Transactional
    public void moveDown(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id); //需要移动的课程
        if (teachplan.getParentid() == 0) {
            //是一个父分类 查询同级的其他父分类
            Long courseId = teachplan.getCourseId();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, 0);
            List<Teachplan> FatherTeachplanList = teachplanMapper.selectList(queryWrapper);

            Optional<Teachplan> optional = FatherTeachplanList.stream()
                    .filter(tp -> tp.getOrderby() == teachplan.getOrderby() + 1).findFirst();
            if (!optional.isPresent()) XueChengPlusException.cast("该章节已无法下移");
            Teachplan ExchangeObjects = optional.get(); //获取要交换排序位置的对象
            Integer temp = teachplan.getOrderby();
            teachplan.setOrderby(ExchangeObjects.getOrderby());
            ExchangeObjects.setOrderby(temp);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(ExchangeObjects);
            return;
        }
        //不是一个父分类
        Long parentId = teachplan.getParentid();
        List<Teachplan> teachplanList = teachplanMapper
                .selectList(Wrappers.<Teachplan>lambdaQuery().eq(Teachplan::getParentid, parentId));
        if (teachplanList.size() <= 1) XueChengPlusException.cast("该课程已无法下移");
        Optional<Teachplan> optional = teachplanList.stream()
                .filter(tp -> tp.getOrderby() == teachplan.getOrderby() + 1).findFirst();
        try {
            Teachplan ExchangeObjects = optional.get();
            Integer temp = teachplan.getOrderby();
            teachplan.setOrderby(ExchangeObjects.getOrderby());
            ExchangeObjects.setOrderby(temp);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(ExchangeObjects);
        }catch (Exception e){
            XueChengPlusException.cast("该课程已在最底部");
        }
    }

    /**
     * 根据课程id 将课程在树中的排序向上移动
     *
     * @param id 课程id
     */
    @Override
    public void moveUp(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id); //需要移动的课程
        if (teachplan.getParentid() == 0) {
            //是一个父分类 查询同级的其他父分类
            Long courseId = teachplan.getCourseId();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, 0);
            List<Teachplan> FatherTeachplanList = teachplanMapper.selectList(queryWrapper);

            Optional<Teachplan> optional = FatherTeachplanList.stream()
                    .filter(tp -> tp.getOrderby() == teachplan.getOrderby() - 1).findFirst();
            if (!optional.isPresent()) XueChengPlusException.cast("该章节已无法上移");
            Teachplan ExchangeObjects = optional.get(); //获取要交换排序位置的对象
            Integer temp = teachplan.getOrderby();
            teachplan.setOrderby(ExchangeObjects.getOrderby());
            ExchangeObjects.setOrderby(temp);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(ExchangeObjects);
            return;
        }
        //不是一个父分类
        Long parentId = teachplan.getParentid();
        List<Teachplan> teachplanList = teachplanMapper
                .selectList(Wrappers.<Teachplan>lambdaQuery().eq(Teachplan::getParentid, parentId));
        if (teachplanList.size() <= 1) XueChengPlusException.cast("该课程已无法上移");
        Optional<Teachplan> optional = teachplanList.stream()
                .filter(tp -> tp.getOrderby() == teachplan.getOrderby() - 1).findFirst();
        try {
            Teachplan ExchangeObjects = optional.get();
            Integer temp = teachplan.getOrderby();
            teachplan.setOrderby(ExchangeObjects.getOrderby());
            ExchangeObjects.setOrderby(temp);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(ExchangeObjects);
        } catch (Exception e) {
            XueChengPlusException.cast("该课程已在最顶部");
        }
    }
}
