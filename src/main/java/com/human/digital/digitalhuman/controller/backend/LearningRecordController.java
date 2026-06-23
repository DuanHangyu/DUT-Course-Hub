package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.StudentAssessAppService;
import com.human.digital.digitalhuman.service.model.request.AssessmentResultListQuery;
import com.human.digital.digitalhuman.service.model.request.LearnRecordListQuery;
import com.human.digital.digitalhuman.service.model.request.LearningRecordModifyDTO;
import com.human.digital.digitalhuman.service.model.response.AssessmentResultDetailDTO;
import com.human.digital.digitalhuman.service.model.response.AssessmentResultSummaryDTO;
import com.human.digital.digitalhuman.service.model.response.LearnRecordSummaryDTO;
import com.human.digital.digitalhuman.service.model.response.StudentCourseDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 10:51 2025/6/15
 */
@RestController
@RequestMapping("/learn-record")
@Tag(name = "后台学习记录管理", description = "后台学习记录管理")
@RequiredArgsConstructor
public class LearningRecordController {

    private final StudentAssessAppService studentAssessAppService;

    @PostMapping("/learn-record-list")
    @Operation(summary = "学习记录列表")
    public IPage<LearnRecordSummaryDTO> learnRecordList(@RequestBody LearnRecordListQuery listQuery){
        return studentAssessAppService.learnRecordList(listQuery);
    }

    @GetMapping("/student-course-detail")
    @Operation(summary = "学生课程详情")
    public List<StudentCourseDetailDTO> studentCourseDetailList(@RequestParam("studentId") Integer studentId){
        return studentAssessAppService.studentCourseDetailList(studentId);
    }

    @PostMapping("/list")
    @Operation(summary = "考核结果列表")
    public IPage<AssessmentResultSummaryDTO> list(@RequestBody AssessmentResultListQuery listQuery){
        return studentAssessAppService.list(listQuery);
    }

    @PostMapping("/modify-check-score")
    @Operation(summary = "修改审核分数")
    public Boolean modifyCheckScore(@RequestBody LearningRecordModifyDTO modifyDTO){
        return studentAssessAppService.modifyCheckScore(modifyDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情查询")
    public AssessmentResultDetailDTO recordDetail(@RequestParam("id") @Schema(description = "学习记录ID") Integer id){
        return studentAssessAppService.recordDetail(id);
    }
}
