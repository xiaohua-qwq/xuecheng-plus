package org.xiaohuadev.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaohuadev.content.mapper.CourseCategoryMapper;
import org.xiaohuadev.content.mapper.TeachplanMapper;
import org.xiaohuadev.content.model.dto.CourseCategoryTreeDto;
import org.xiaohuadev.content.model.dto.TeachplanDto;

import java.util.List;

@SpringBootTest
public class TeachPlanMapperTests {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    void testSelectTreeNodes() {
        List<TeachplanDto> teachplanDtos = teachplanMapper.teachTreeNodes(117L);
        System.out.println(teachplanDtos);
    }
}
