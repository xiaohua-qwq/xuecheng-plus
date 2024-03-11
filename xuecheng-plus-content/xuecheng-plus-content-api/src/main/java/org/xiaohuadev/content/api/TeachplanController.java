package org.xiaohuadev.content.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaohuadev.content.model.dto.BindTeachplanMediaDto;
import org.xiaohuadev.content.model.dto.SaveTeachplanDto;
import org.xiaohuadev.content.model.dto.TeachplanDto;
import org.xiaohuadev.content.service.TeachplanService;

import java.util.List;

/**
 * 课程计划管理相关接口
 */
@RestController
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{courseId}")
    public void deleteTeachplan(@PathVariable Long courseId) {
        teachplanService.deleteTeachplan(courseId);
    }

    @ApiOperation("下移课程")
    @PostMapping("/teachplan/movedown/{courseId}")
    public void moveDown(@PathVariable Long courseId) {
        teachplanService.moveDown(courseId);
    }

    @ApiOperation("上移课程")
    @PostMapping("/teachplan/moveup/{courseId}")
    public void moveUp(@PathVariable Long courseId) {
        teachplanService.moveUp(courseId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }


}
