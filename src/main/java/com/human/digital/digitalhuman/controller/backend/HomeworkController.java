package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.HomeworkAppService;
import com.human.digital.digitalhuman.service.model.request.HomeworkCreateCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkDeleteCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkModifyCmd;
import com.human.digital.digitalhuman.service.model.response.HomeworkListDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkSubmitDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业管理控制器（教师端）
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@RestController
@RequestMapping("/backend/homework")
@RequiredArgsConstructor
@Tag(name = "后台作业管理", description = "后台作业管理")
@Validated
public class HomeworkController {

    private final HomeworkAppService homeworkAppService;

    @GetMapping("/list")
    @Operation(summary = "作业列表")
    public List<HomeworkListDTO> list(
            @Parameter(description = "课程ID") @RequestParam("courseId") Integer courseId) {
        return homeworkAppService.list(courseId);
    }

    @PostMapping("/create")
    @Operation(summary = "创建作业")
    public Boolean create(@Valid @RequestBody HomeworkCreateCmd cmd) {
        return homeworkAppService.create(cmd);
    }

    @PostMapping("/modify")
    @Operation(summary = "修改作业")
    public Boolean modify(@Valid @RequestBody HomeworkModifyCmd cmd) {
        return homeworkAppService.modify(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除作业")
    public Boolean delete(@Valid @RequestBody HomeworkDeleteCmd cmd) {
        return homeworkAppService.delete(cmd);
    }

    @GetMapping("/submit-detail")
    @Operation(summary = "提交详情")
    public List<HomeworkSubmitDetailDTO> submitDetail(
            @Parameter(description = "作业ID") @RequestParam("homeworkId") Integer homeworkId) {
        return homeworkAppService.submitDetail(homeworkId);
    }
}
