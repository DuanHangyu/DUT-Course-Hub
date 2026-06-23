package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.OverviewAppService;
import com.human.digital.digitalhuman.service.model.dto.*;
import com.human.digital.digitalhuman.service.model.query.OverviewQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据概览控制器
 *
 * @Author taoHouChao
 * @Date 10:40 2026/3/13
 */
@RestController
@RequestMapping("/backend/overview")
@RequiredArgsConstructor
@Tag(name = "数据概览", description = "学校数据概览看板接口")
@Validated
public class OverviewController {

    private final OverviewAppService overviewAppService;

    @GetMapping("/summary")
    @Operation(summary = "获取概览摘要")
    public OverviewSummaryDTO getSummary(
            @Parameter(description = "学校ID") @RequestParam("schoolId") Integer schoolId) {
        OverviewQuery query = new OverviewQuery();
        query.setSchoolId(schoolId);
        return overviewAppService.getSummary(query);
    }

    @GetMapping("/active-trend/24h")
    @Operation(summary = "获取24小时活跃趋势")
    public TrendDataDTO getActiveTrend24h(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getActiveTrend24h(schoolId);
    }

    @GetMapping("/homework-trend/7d")
    @Operation(summary = "获取作业提交趋势（近7天）")
    public TrendDataDTO getHomeworkTrend7d(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getHomeworkTrend7d(schoolId);
    }

    @GetMapping("/ai-response-trend/7d")
    @Operation(summary = "获取AI响应趋势（近7天）")
    public TrendDataDTO getAiResponseTrend7d(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getAiResponseTrend7d(schoolId);
    }

    @GetMapping("/risk-trend/10d")
    @Operation(summary = "获取风险预警趋势（近10天）")
    public TrendDataDTO getRiskTrend10d(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getRiskTrend10d(schoolId);
    }

    @GetMapping("/heatmap")
    @Operation(summary = "获取学习时间热力图")
    public HeatmapDTO getHeatmap(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId,
            @Parameter(description = "开始日期") @RequestParam(value = "startDate", required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        OverviewQuery query = new OverviewQuery();
        query.setSchoolId(schoolId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        return overviewAppService.getHeatmap(query);
    }

    @GetMapping("/class-progress")
    @Operation(summary = "获取班级学习进度")
    public List<ClassProgressDTO> getClassProgress(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getClassProgress(schoolId);
    }

    @GetMapping("/trend")
    @Operation(summary = "获取活跃度趋势（近30天）")
    public TrendDataDTO getTrend(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getTrend(schoolId);
    }

    @GetMapping("/popular-courses")
    @Operation(summary = "获取热门课程（Top3）")
    public List<PopularCourseDTO> getPopularCourses(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getPopularCourses(schoolId);
    }

    @GetMapping("/course-completion")
    @Operation(summary = "获取课程完成度")
    public List<CourseCompletionItemDTO> getCourseCompletion(
            @Parameter(description = "学校ID") @RequestParam(value = "schoolId") Integer schoolId) {
        return overviewAppService.getCourseCompletion(schoolId);
    }
}
