package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.CourseAppService;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.CourseNodeDTO;
import com.human.digital.digitalhuman.service.model.response.CourseSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 19:33 2025/6/11
 */
@RestController
@RequestMapping("/course")
@Tag(name = "后台课程管理", description = "后台课程管理")
public class CourseController {

    @Resource
    private CourseAppService courseAppService;

    @PostMapping("/page-query")
    @Operation(summary = "分页查询")
    public IPage<CourseSummaryDTO> pageQuery(@RequestBody CourseListQuery query){
        return courseAppService.pageQuery(query);
    }

    @PostMapping("/create")
    @Operation(summary = "创建课程")
    public Boolean create(@RequestBody CourseCreateCmd cmd){
        return courseAppService.create(cmd);
    }

    @PostMapping("/modify")
    @Operation(summary = "修改课程")
    public Boolean modify(@RequestBody CourseModifyCmd cmd){
        return courseAppService.modify(cmd);
    }

    @PostMapping("/change-state")
    @Operation(summary = "修改课程状态")
    public Boolean changeStatus(@RequestBody CourseModifyCmd cmd){
        return courseAppService.changeState(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除课程")
    public Boolean delete(@RequestBody CourseModifyCmd cmd){
        return courseAppService.delete(cmd);
    }

    @PostMapping("/node-create")
    @Operation(summary = "创建课程节点")
    public Integer createNode(@RequestBody CourseNodeCreateCmd createCmd){
        return courseAppService.createNode(createCmd);
    }

    @PostMapping("/node-modify")
    @Operation(summary = "修改课程节点")
    public Boolean modifyNode(@RequestBody CourseNodeModifyCmd modifyCmd){
        return courseAppService.modifyNode(modifyCmd);
    }

    @PostMapping("/node-delete")
    @Operation(summary = "删除课程节点")
    public Boolean deleteNode(@RequestBody CourseNodeModifyCmd modifyCmd){
        return courseAppService.deleteNode(modifyCmd);
    }

    @PostMapping("/node-move")
    @Operation(summary = "移动课程节点")
    public Boolean moveNode(@RequestBody CourseNodeMoveCmd moveCmd){
        return courseAppService.moveNode(moveCmd);
    }

    @GetMapping("/course-node")
    @Operation(summary = "查询课程节点")
    public List<CourseNodeDTO> queryNode(@RequestParam("courseId") Integer courseId){
        return courseAppService.queryNode(courseId);
    }
}
