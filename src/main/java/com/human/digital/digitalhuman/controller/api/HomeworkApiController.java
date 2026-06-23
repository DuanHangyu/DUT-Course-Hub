package com.human.digital.digitalhuman.controller.api;

import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.HomeworkApiAppService;
import com.human.digital.digitalhuman.service.model.request.HomeworkSubmitCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkSubmitDeleteCmd;
import com.human.digital.digitalhuman.service.model.response.HomeworkDetailDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkStudentListDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业管理控制器（学生端）
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
@Tag(name = "前台作业管理", description = "前台作业管理")
@Validated
public class HomeworkApiController {

    private final HomeworkApiAppService homeworkApiAppService;

    @GetMapping("/list")
    @Operation(summary = "作业列表")
    @OperateLog("作业列表")
    public List<HomeworkStudentListDTO> list(
            @Parameter(description = "课程ID") @RequestParam("courseId") Integer courseId) {
        return homeworkApiAppService.list(courseId);
    }

    @GetMapping("/detail")
    @Operation(summary = "作业详情")
    @OperateLog("作业详情")
    public HomeworkDetailDTO detail(
            @Parameter(description = "作业ID") @RequestParam("homeworkId") Integer homeworkId) {
        return homeworkApiAppService.detail(homeworkId);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交作业")
    @OperateLog("提交作业")
    public Boolean submit(@Valid @RequestBody HomeworkSubmitCmd cmd) {
        return homeworkApiAppService.submit(cmd);
    }

    @GetMapping("/my-submit")
    @Operation(summary = "我的提交")
    public HomeworkDetailDTO mySubmit(
            @Parameter(description = "作业ID") @RequestParam("homeworkId") Integer homeworkId) {
        return homeworkApiAppService.mySubmit(homeworkId);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除提交")
    public Boolean delete(@RequestBody HomeworkSubmitDeleteCmd deleteCmd) {
        return homeworkApiAppService.delete(deleteCmd);
    }
}
