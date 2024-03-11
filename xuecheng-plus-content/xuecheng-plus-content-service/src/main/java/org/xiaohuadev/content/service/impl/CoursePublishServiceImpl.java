package org.xiaohuadev.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaohuadev.base.exception.XueChengPlusException;
import org.xiaohuadev.content.mapper.CourseBaseMapper;
import org.xiaohuadev.content.mapper.CourseMarketMapper;
import org.xiaohuadev.content.mapper.CoursePublishPreMapper;
import org.xiaohuadev.content.model.dto.CourseBaseInfoDto;
import org.xiaohuadev.content.model.dto.CoursePreviewDto;
import org.xiaohuadev.content.model.dto.TeachplanDto;
import org.xiaohuadev.content.model.po.CourseBase;
import org.xiaohuadev.content.model.po.CourseMarket;
import org.xiaohuadev.content.model.po.CoursePublishPre;
import org.xiaohuadev.content.service.CourseBaseInfoService;
import org.xiaohuadev.content.service.CoursePublishService;
import org.xiaohuadev.content.service.TeachplanService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 根据课程id返回课程信息预览数据模型
     *
     * @param courseId 课程id
     * @return 课程信息预览数据模型
     */
    @Override
    public CoursePreviewDto preview(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //获取课程基本信息和课程营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        //获取课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }

    /**
     * 提交审核
     *
     * @param companyId 机构id
     * @param courseId  课程iid
     */
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //检查课程是否满足提交条件
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) XueChengPlusException.cast("找不到目标课程");

        //校验所属机构是否为本机构
        if (!courseBaseInfo.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("无权修改其他机构的课程");
        }

        //检查课程状态是否为已提交 如果是则不允许提交
        String auditStatus = courseBaseInfo.getAuditStatus();
        if (auditStatus.equals("202003")) XueChengPlusException.cast("课程已提交 请等待审核");

        //课程图片 计划等信息是否已填写 未填写则不能提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("课程没有图片信息");
        }
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.isEmpty()) {
            XueChengPlusException.cast("课程计划不能为空");
        }

        //组装课程信息(预发布表po类)
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        coursePublishPre.setCompanyId(companyId); //设置课程机构id
        //组装课程营销信息(JSON)
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //组装课程计划信息(JSON)
        String teachplanJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanJson);
        //设置状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //将课程信息插入到预发布表
        //查询预发布表 如果有记录则更新 没有则插入
        int flag = -1;
        CoursePublishPre coursePublishPreFromDB = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreFromDB == null) { //插入
            flag = coursePublishPreMapper.insert(coursePublishPre);
        } else { //更新
            BeanUtils.copyProperties(coursePublishPre, coursePublishPreFromDB);
            flag = coursePublishPreMapper.updateById(coursePublishPreFromDB);
        }
        if (flag <= 0) XueChengPlusException.cast("插入课程信息时出错");

        //更新课程基本信息表状态为审核中
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003"); //已提交
        flag = courseBaseMapper.updateById(courseBase);
        if (flag <= 0) XueChengPlusException.cast("更新课程状态时出错");
    }
}
