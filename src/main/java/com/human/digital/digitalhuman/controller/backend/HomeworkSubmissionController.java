package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.HomeworkAppService;
import com.human.digital.digitalhuman.service.model.dto.ClassDTO;
import com.human.digital.digitalhuman.service.model.request.HomeworkGradeCmd;
import com.human.digital.digitalhuman.service.model.response.HomeworkSubmissionListDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkSubmissionStatisticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业提交管理控制器（教师端）
 *
 * @Author taoHouChao
 * @Date 2025/03/12
 */
@RestController
@RequestMapping("/backend/homework/submission")
@RequiredArgsConstructor
@Tag(name = "后台作业提交管理", description = "后台作业提交管理")
@Validated
public class HomeworkSubmissionController {

    private final HomeworkAppService homeworkAppService;

    @GetMapping("/statistics")
    @Operation(summary = "提交统计")
    public HomeworkSubmissionStatisticsDTO statistics(
            @Parameter(description = "作业ID") @RequestParam("homeworkId") Integer homeworkId) {
        return homeworkAppService.submissionStatistics(homeworkId);
    }

    @GetMapping("/classes")
    @Operation(summary = "获取作业关联的班级列表")
    public List<ClassDTO> classes(
            @Parameter(description = "作业ID") @RequestParam("homeworkId") Integer homeworkId) {
        return homeworkAppService.getClassesByHomeworkId(homeworkId);
    }

    @GetMapping("/list")
    @Operation(summary = "提交列表")
    public List<HomeworkSubmissionListDTO> list(
            @Parameter(description = "作业ID") @RequestParam("homeworkId") Integer homeworkId,
            @Parameter(description = "学生姓名") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "学号") @RequestParam(value = "studentNo", required = false) String studentNo,
            @Parameter(description = "班级ID") @RequestParam(value = "classId", required = false) Integer classId,
            @Parameter(description = "提交状态") @RequestParam(value = "status", required = false) String status) {
        return homeworkAppService.submissionList(homeworkId, name, studentNo, classId, status);
    }

    @PostMapping("/grade")
    @Operation(summary = "批改作业")
    public Boolean grade(@Valid @RequestBody HomeworkGradeCmd cmd) {
        return homeworkAppService.grade(cmd);
    }

    @PutMapping("/regrade")
    @Operation(summary = "重新批改")
    public Boolean regrade(@Valid @RequestBody HomeworkGradeCmd cmd) {
        return homeworkAppService.regrade(cmd);
    }
}
