package org.xiaohuadev.content.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaohuadev.content.mapper.CourseCategoryMapper;
import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;
import org.xiaohuadev.content.model.po.CourseCategory;
import org.xiaohuadev.content.service.CourseCategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 课程分类树形结构查询
     *
     * @param rootId 根节点id
     * @return 根据根节点id查询到的子节点集合
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String rootId) {
        //查询根节点的所有子节点
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(rootId);

        //将节点转换为Map key为节点的id value为CourseCategoryTreeDto
        Map<String, CourseCategoryTreeDto> treeDtoMap = courseCategoryTreeDtos.stream().filter
                        (item -> !rootId.equals(item.getId()))
                .collect(Collectors.toMap(
                        CourseCategoryTreeDto::getId,
                        CourseCategoryTreeDto -> CourseCategoryTreeDto,
                        (CourseCategoryId1, CourseCategoryId2) -> CourseCategoryId2
                ));

        //遍历List<CourseCategoryTreeDto> 找到每个节点的子节点
        List<CourseCategoryTreeDto> CourseCategoryResultList = new ArrayList<>();
        courseCategoryTreeDtos.stream().filter(item -> !rootId.equals(item.getId())).forEach(item -> {
            //如果该条件成立 那么代表他是根节点下的直接子节点 同时是需要返回的节点列表中最底层节点中的一个
            if (item.getParentid().equals(rootId)) {
                CourseCategoryResultList.add(item); //直接加入到返回节点列表中
            }

            CourseCategoryTreeDto courseCategoryParent = treeDtoMap.get(item.getParentid());
            if (courseCategoryParent != null) { //如果父节点不为空 则代表他拥有父节点 也代表他不是需要返回的根节点
                if (courseCategoryParent.getChildrenTreeNode() == null) { //父节点的子节点列表为空 则创建
                    courseCategoryParent.setChildrenTreeNode(new ArrayList<CourseCategoryTreeDto>());
                }
                //从父节点中获取子节点列表 因为能运行到这一步 代表他的父节点不是根节点 则添加自身
                courseCategoryParent.getChildrenTreeNode().add(item); //将自身添加到父节点的子节点列表中
            }
        });

        return CourseCategoryResultList;
    }
}
