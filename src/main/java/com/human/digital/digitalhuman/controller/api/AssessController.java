package com.human.digital.digitalhuman.controller.api;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.AssessService;
import com.human.digital.digitalhuman.service.StudentAssessAppService;
import com.human.digital.digitalhuman.service.model.dto.AnswerEvaluationSimpleDTO;
import com.human.digital.digitalhuman.service.model.request.AnswerSaveCmd;
import com.human.digital.digitalhuman.service.model.request.AssessEndCmd;
import com.human.digital.digitalhuman.service.model.request.StartFinalAssessCmd;
import com.human.digital.digitalhuman.service.model.request.StartNodeAssessCmd;
import com.human.digital.digitalhuman.service.model.response.AssessDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @USER taoHouChao
 * @DATE 22:30 2025/6/15
 */
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台考核管理", description = "前台考核管理")
@Validated
public class AssessController {

    private final AssessService assessService;

    private final StudentAssessAppService studentAssessAppService;


    @PostMapping("/start-node-assess")
    @Operation(summary = "课程节点开始考核")
    @OperateLog("课程节点开始考核")
    public Boolean nodeStartAssess(@RequestBody StartNodeAssessCmd startAssessCmd){
        return assessService.nodeStartAssess(startAssessCmd.getCourseNodeId(), StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/start-final-assess")
    @Operation(summary = "课程最终考核开始")
    @OperateLog("课程最终考核开始")
    public Boolean startFinalAssess(@RequestBody StartFinalAssessCmd startAssessCmd){
        return assessService.startFinalAssess(startAssessCmd.getCourseId(), StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/answer-save")
    @Operation(summary = "保存问答")
    @OperateLog("保存问答")
    public Boolean saveAnswer(@RequestBody AnswerSaveCmd saveCmd){
        return assessService.saveAnswer(saveCmd);
    }

    @PostMapping("/check-able-end-assess")
    @Operation(summary = "检查是否可以结束考核接口")
    @OperateLog("检查是否可以结束考核接口")
    public Boolean checkAbleEndAssess(@RequestBody AssessEndCmd endCmd){
        return assessService.checkAbleEndAssess(endCmd);
    }

    @PostMapping("/end-assess")
    @Operation(summary = "结束考核接口")
    @OperateLog("结束考核接口")
    public Boolean endAssess(@RequestBody AssessEndCmd endCmd){
        return assessService.endAssess(StpUtil.getLoginIdAsInt(), endCmd);
    }

    @GetMapping("/assess-detail")
    @Operation(summary = "考核详情接口")
    public AssessDetailDTO nodeAssessDetail(@RequestParam("courseNodeId") @Schema(description = "课程节点ID") Integer courseNodeId){
        return studentAssessAppService.assessDetail(courseNodeId);
    }

    @GetMapping("/course-assess-detail")
    @Operation(summary = "课程考核详情接口")
    public AssessDetailDTO courseAssessDetail(@RequestParam("courseId") @Schema(description = "课程ID") Integer courseId){
        return studentAssessAppService.courseAssessDetail(courseId);
    }

    @GetMapping("/analysis")
    @Operation(summary = "答案评估接口")
    public AnswerEvaluationSimpleDTO analysis(@RequestParam("assessDetailId") Integer assessDetailId){
        return studentAssessAppService.analysis(assessDetailId);
    }
}
