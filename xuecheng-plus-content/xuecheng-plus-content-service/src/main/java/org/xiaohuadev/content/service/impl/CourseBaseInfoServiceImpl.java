package org.xiaohuadev.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.model.dto.TeachplanDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.mapper.*;
import org.xiaohuadev.content.model.dto.AddCourseDto;
import org.xiaohuadev.content.model.dto.CourseBaseInfoDto;
import org.xiaohuadev.content.model.dto.EditCourseDto;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.*;
import org.xiaohuadev.content.service.CourseBaseInfoService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * 课程信息查询接口
     *
     * @param pageParams      分页信息
     * @param courseParamsDto 查询条件
     * @return 分页数据返回类
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //组装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),
                CourseBase::getName, courseParamsDto.getCourseName()); //课程名称

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus, courseParamsDto.getAuditStatus()); //课程审核状态

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()),
                CourseBase::getStatus, courseParamsDto.getPublishStatus()); //课程发布状态

        //组装分页条件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //从数据库进行分页查询
        Page<CourseBase> result = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = result.getRecords();
        long total = result.getTotal();

        return new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 新增课程
     *
     * @param companyId 机构id
     * @param dto       要添加的课程信息
     * @return 添加到数据库的课程信息
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        dto.setSt("1-1-1"); //小分类前端故障 暂时硬编码
        //向课程基本信息course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //审核状态设置为未提交
        courseBaseNew.setAuditStatus("202002");
        //发布状态设置为未发布
        courseBaseNew.setStatus("203001");
        //插入数据库
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {
            //throw new RuntimeException("课程信息插入数据库时异常");
            XueChengPlusException.cast("课程信息插入数据库时异常");
        }

        //向课程营销表course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto, courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if (i <= 0) {
            //throw new RuntimeException("保存课程营销信息失败");
            XueChengPlusException.cast("保存课程营销信息失败");
        }

        //从数据库中提取课程信息和营销信息并组装
        return this.getCourseBaseInfo(courseId);
    }

    public int saveCourseMarket(CourseMarket courseMarketNew) {
        //参数的合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isEmpty(charge)) {
            //throw new RuntimeException("收费规则为空");
            XueChengPlusException.cast("收费规则为空");
        }

        //如果课程收费 校验课程的收费参数是否合法
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0) {
                //throw new RuntimeException("课程的价格不能为空且必须大于0");
                XueChengPlusException.cast("课程的价格不能为空且必须大于0");
            }
        }

        //从数据库中查询营销信息 存在则更新 不存在则添加
        CourseMarket courseMarket = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarket == null) {
            return courseMarketMapper.insert(courseMarketNew); //插入数据库并返回插入的行
        } else {
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarket);
        }
    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //组装两个信息
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //查询课程分类信息并组装
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());


        return courseBaseInfoDto;
    }

    /**
     * 根据课程id修改课程
     *
     * @param companyId     机构id
     * @param editCourseDto 要修改的课程信息
     * @return 修改后的课程详细信息
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {

        //从数据库中查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(editCourseDto.getId());
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        //参数合法性校验
        //由于controller层已完成基本的参数校验 Service需要进行逻辑校验
        //校验当前机构id和要修改的课程所属的机构id是否相等
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("无权修改其他机构的课程信息");
        }

        //复制传入的课程信息到数据库课程信息中
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now()); //设置修改时间

        //更新数据库
        int updateLine = courseBaseMapper.updateById(courseBase);
        if (updateLine <= 0) {
            XueChengPlusException.cast("更新课程信息失败");
        }

        //更新营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(editCourseDto.getId());
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        int courseMarketUpdateLine = courseMarketMapper.updateById(courseMarket);
        if (courseMarketUpdateLine <= 0) {
            XueChengPlusException.cast("更新课程营销信息失败");
        }

        //查询数据并返回
        return this.getCourseBaseInfo(editCourseDto.getId());
    }

    /**
     * 根据课程id删除课程
     *
     * @param courseId 课程id
     */
    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        //删除教学计划信息
        teachplanMapper.delete(Wrappers.<Teachplan>lambdaQuery().eq(Teachplan::getCourseId, courseId));
        //删除教师信息
        courseTeacherMapper.delete(Wrappers.<CourseTeacher>lambdaQuery()
                .eq(CourseTeacher::getCourseId, courseId));
        //删除课程营销信息
        courseMarketMapper.delete(Wrappers.<CourseMarket>lambdaQuery().eq(CourseMarket::getId, courseId));
        //删除课程信息
        int line = courseBaseMapper.deleteById(courseId);
        if (line <= 0) XueChengPlusException.cast("删除课程信息失败");
    }
}
