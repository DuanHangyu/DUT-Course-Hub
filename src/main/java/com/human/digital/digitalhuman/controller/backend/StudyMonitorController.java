package com.human.digital.digitalhuman.controller.backend;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.service.StudyMonitorAppService;
import com.human.digital.digitalhuman.service.model.request.RiskWarningHandleCmd;
import com.human.digital.digitalhuman.service.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学情监控控制器
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@RestController
@RequestMapping("/api/study-monitor")
@RequiredArgsConstructor
@Tag(name = "学情监控", description = "学情监控看板接口")
@Validated
public class StudyMonitorController {

    private final StudyMonitorAppService studyMonitorAppService;

    @GetMapping("/overview")
    @Operation(summary = "获取核心指标概览")
    public CoreIndicatorOverviewDTO getCoreIndicatorOverview(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getCoreIndicatorOverview(courseId, teacherId);
    }

    @GetMapping("/progress-trend")
    @Operation(summary = "获取进度趋势")
    public ProgressTrendDTO getProgressTrend(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getProgressTrend(courseId, teacherId);
    }

    @GetMapping("/knowledge-graph")
    @Operation(summary = "获取知识图谱热力图")
    public KnowledgeGraphDTO getKnowledgeGraph(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getKnowledgeGraph(courseId, teacherId);
    }

    @GetMapping("/blind-spots")
    @Operation(summary = "获取高频知识盲区")
    public BlindSpotDTO getBlindSpots(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getBlindSpots(courseId, teacherId);
    }

    @GetMapping("/node-detail/{nodeId}")
    @Operation(summary = "获取节点详情")
    public NodeDetailDTO getNodeDetail(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId,
            @Parameter(description = "节点ID") @PathVariable("nodeId") Long nodeId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getNodeDetail(courseId, nodeId, teacherId);
    }

    @GetMapping("/activity-ranking")
    @Operation(summary = "获取问题活跃度排行")
    public List<ActivityRankingDTO> getActivityRanking(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getActivityRanking(courseId, teacherId);
    }

    @GetMapping("/activity-heatmap")
    @Operation(summary = "获取学习活力热力图")
    public ActivityHeatmapDTO getActivityHeatmap(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getActivityHeatmap(courseId, teacherId);
    }

    @GetMapping("/time-distribution")
    @Operation(summary = "获取学习时段分布（24小时）")
    public Map<Integer, Integer> getTimeDistribution(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getLearningTimeDistribution(courseId, teacherId);
    }

    @GetMapping("/progress-ranking")
    @Operation(summary = "获取学生进度排行榜 Top10")
    public List<StudentProgressDTO> getProgressRanking(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getProgressRanking(courseId, teacherId);
    }

    @GetMapping("/chapter-efficiency")
    @Operation(summary = "获取章节效率分布")
    public ChapterEfficiencyDTO getChapterEfficiency(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getChapterEfficiency(courseId, teacherId);
    }

    @GetMapping("/course-overview")
    @Operation(summary = "获取课程作业统计列表")
    public List<HomeworkStatisticsDTO> getCourseOverview(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getCourseOverview(courseId, teacherId);
    }

    @GetMapping("/task-flow")
    @Operation(summary = "获取任务完成流程图")
    public TaskFlowDTO getTaskFlow(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId,
            @Parameter(description = "学生ID") @RequestParam("studentId") Long studentId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getTaskFlow(courseId, studentId, teacherId);
    }

    @GetMapping("/node-students/{nodeId}")
    @Operation(summary = "获取节点学生列表")
    public NodeStudentsDTO getNodeStudents(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId,
            @Parameter(description = "节点ID") @PathVariable("nodeId") Long nodeId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getNodeStudents(courseId, nodeId, teacherId);
    }

    @GetMapping("/risk-warnings")
    @Operation(summary = "获取风险预警列表")
    public RiskWarningListDTO getRiskWarnings(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getRiskWarnings(courseId, teacherId);
    }

    @PutMapping("/risk-warnings/{id}")
    @Operation(summary = "处理风险预警")
    public Boolean handleRiskWarning(
            @Parameter(description = "预警ID") @PathVariable("id") Long id,
            @Valid @RequestBody RiskWarningHandleCmd cmd) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.handleRiskWarning(id, teacherId);
    }

    @GetMapping("/student-progress")
    @Operation(summary = "获取学生进度列表")
    public StudentProgressListVO getStudentProgressList(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId,
            @Parameter(description = "筛选类型") @RequestParam(value = "filterType", required = false) String filterType,
            @Parameter(description = "学生姓名") @RequestParam(value = "studentName", required = false) String studentName,
            @Parameter(description = "学号") @RequestParam(value = "studentNo", required = false) String studentNo,
            @Parameter(description = "班级名称") @RequestParam(value = "className", required = false) String className,
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getStudentProgressList(courseId, teacherId, filterType,
                studentName, studentNo, className, pageNum, pageSize);
    }

    @GetMapping("/student/{studentId}/learning-detail")
    @Operation(summary = "获取学生学习详情")
    public StudentLearningDetailVO getStudentLearningDetail(
            @Parameter(description = "课程ID") @RequestParam("courseId") Long courseId,
            @Parameter(description = "学生ID") @PathVariable("studentId") Long studentId) {
        long teacherId = StpUtil.getLoginIdAsLong();
        return studyMonitorAppService.getStudentLearningDetail(studentId, courseId, teacherId);
    }
}
