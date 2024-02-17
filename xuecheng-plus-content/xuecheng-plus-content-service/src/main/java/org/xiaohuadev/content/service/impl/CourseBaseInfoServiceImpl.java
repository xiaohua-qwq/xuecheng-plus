package org.xiaohuadev.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.base.model.PageParams;
import org.xiaohuadev.base.model.PageResult;
import org.xiaohuadev.content.mapper.CourseBaseMapper;
import org.xiaohuadev.content.mapper.CourseCategoryMapper;
import org.xiaohuadev.content.mapper.CourseMarketMapper;
import org.xiaohuadev.content.model.dto.AddCourseDto;
import org.xiaohuadev.content.model.dto.CourseBaseInfoDto;
import org.xiaohuadev.content.model.dto.QueryCourseParamsDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.model.po.CourseCategory;
import org.xiaohuadev.content.model.po.CourseMarket;
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

        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName()); //课程名称

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus()); //课程审核状态

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus()); //课程发布状态

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
        //参数的合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            XueChengPlusException.cast("课程名称为空");
            //throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            XueChengPlusException.cast("课程分类为空");
            //throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            //throw new RuntimeException("课程分类为空");
            dto.setSt("1-1-9");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            XueChengPlusException.cast("课程等级为空");
            //throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            XueChengPlusException.cast("教育模式为空");
            //throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            XueChengPlusException.cast("适应人群为空");
            //throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            XueChengPlusException.cast("收费规则为空");
            //throw new RuntimeException("收费规则为空");
        }

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

    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
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
}
