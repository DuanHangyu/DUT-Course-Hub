package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.CourseTemplateAppService;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.CourseNodeDTO;
import com.human.digital.digitalhuman.service.model.response.CourseTemplateDTO;
import com.human.digital.digitalhuman.service.model.response.CourseTemplateStatusCountDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板课程控制器
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@RestController
@RequestMapping("/course-template")
@Tag(name = "模板课程管理", description = "模板课程管理接口")
@Validated
public class CourseTemplateController {

    @Resource
    private CourseTemplateAppService courseTemplateAppService;

    @PostMapping("/page-query")
    @Operation(summary = "分页查询")
    public IPage<CourseTemplateDTO> pageQuery(@RequestBody CourseTemplateQuery query) {
        return courseTemplateAppService.pageQuery(query);
    }

    @PostMapping("/create")
    @Operation(summary = "创建模板课程")
    public Boolean create(@RequestBody @Valid CourseTemplateCmd cmd) {
        return courseTemplateAppService.create(cmd);
    }

    @PostMapping("/modify")
    @Operation(summary = "编辑模板课程")
    public Boolean modify(@RequestBody @Valid CourseTemplateCmd cmd) {
        return courseTemplateAppService.modify(cmd);
    }

    @GetMapping("/delete")
    @Operation(summary = "删除模板课程")
    public Boolean delete(@RequestParam("id") Integer id) {
        return courseTemplateAppService.delete(id);
    }

    @GetMapping("/detail")
    @Operation(summary = "课程详情")
    public CourseTemplateDTO detail(@RequestParam("id") Integer id) {
        return courseTemplateAppService.detail(id);
    }

    @GetMapping("/nodes")
    @Operation(summary = "获取课程节点列表")
    public List<CourseNodeDTO> listNodes(@RequestParam("courseId") Integer courseId) {
        return courseTemplateAppService.listNodes(courseId);
    }

    @PostMapping("/node-create")
    @Operation(summary = "创建课程节点")
    public Integer createNode(@RequestBody @Valid CourseNodeCreateCmd createCmd) {
        return courseTemplateAppService.createNode(createCmd);
    }

    @PostMapping("/node-modify")
    @Operation(summary = "编辑课程节点")
    public Boolean modifyNode(@RequestBody @Valid CourseNodeModifyCmd modifyCmd) {
        return courseTemplateAppService.modifyNode(modifyCmd);
    }

    @PostMapping("/node-delete")
    @Operation(summary = "删除课程节点")
    public Boolean deleteNode(@RequestBody @Valid CourseNodeModifyCmd modifyCmd) {
        return courseTemplateAppService.deleteNode(modifyCmd);
    }

    @PostMapping("/node-move")
    @Operation(summary = "移动课程节点")
    public Boolean moveNode(@RequestBody @Valid CourseNodeMoveCmd moveCmd) {
        return courseTemplateAppService.moveNode(moveCmd);
    }

    @GetMapping("/subject-list")
    @Operation(summary = "获取学科列表")
    public List<String> subjectList(){
        return courseTemplateAppService.subjectList();
    }

    @GetMapping("/status-count")
    @Operation(summary = "获取课程模板状态统计")
    public CourseTemplateStatusCountDTO statusCount(){
        return courseTemplateAppService.statusCount();
    }
}
