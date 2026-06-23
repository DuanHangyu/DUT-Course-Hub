package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.UniversityOverviewAppService;
import com.human.digital.digitalhuman.service.model.dto.university.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大学端数据概览控制器
 * 提供大学端数据概览的各类统计和查询接口
 *
 * @Author taoHouChao
 * @Date 22:50 2026/3/13
 */
@RestController
@RequestMapping("/backend/university-overview")
@RequiredArgsConstructor
@Tag(name = "大学端数据概览", description = "大学端数据概览看板接口")
@Validated
public class UniversityOverviewController {

    private final UniversityOverviewAppService universityOverviewAppService;

    /**
     * 获取核心统计数据
     * 统计学校数、学生总数、课程总数和AI算力消耗
     *
     * @return 核心统计数据
     */
    @GetMapping("/core-statistics")
    @Operation(summary = "获取核心统计数据")
    public CoreStatisticsDTO getCoreStatistics() {
        return universityOverviewAppService.getCoreStatistics();
    }

    /**
     * 获取学校活跃榜
     * 查询启用的学校，按活跃度排序返回Top5
     *
     * @return 学校排名列表
     */
    @GetMapping("/school-ranking")
    @Operation(summary = "获取学校活跃榜")
    public List<SchoolRankingDTO> getSchoolRanking() {
        return universityOverviewAppService.getSchoolRanking();
    }

    /**
     * 获取学习趋势
     * 生成近7天日期列表，并为每个学校生成趋势线数据
     *
     * @return 学习趋势数据
     */
    @GetMapping("/learning-trend")
    @Operation(summary = "获取学习趋势")
    public LearningTrendDTO getLearningTrend() {
        return universityOverviewAppService.getLearningTrend();
    }

    /**
     * 获取实时活跃数据
     * 当前活跃学生数（最近5分钟有操作记录）、当前活跃学校数、当前覆盖课程数
     *
     * @return 实时活跃数据
     */
    @GetMapping("/realtime-data")
    @Operation(summary = "获取实时活跃数据")
    public RealtimeDataDTO getRealtimeData() {
        return universityOverviewAppService.getRealtimeData();
    }

    /**
     * 获取24小时流量趋势
     * 按小时统计过去24小时的访问日志
     *
     * @return 24小时流量数据
     */
    @GetMapping("/24h-traffic")
    @Operation(summary = "获取24小时流量趋势")
    public Traffic24hDTO getTraffic24h() {
        return universityOverviewAppService.getTraffic24h();
    }

    /**
     * 获取学校区域分布
     * 按学校region字段分组统计
     *
     * @return 学校区域分布数据
     */
    @GetMapping("/school-region")
    @Operation(summary = "获取学校区域分布")
    public SchoolRegionDTO getSchoolRegion() {
        return universityOverviewAppService.getSchoolRegion();
    }

    /**
     * 获取课程学习统计
     * 按学习人数排序返回Top5
     *
     * @return 课程学习统计数据
     */
    @GetMapping("/course-learning")
    @Operation(summary = "获取课程学习统计")
    public CourseLearningDTO getCourseLearning() {
        return universityOverviewAppService.getCourseLearning();
    }

    /**
     * 获取系统健康状态
     * 返回默认健康状态（API响应、数据库连接、服务器负载）
     *
     * @return 系统健康状态数据
     */
    @GetMapping("/system-health")
    @Operation(summary = "获取系统健康状态")
    public SystemHealthDTO getSystemHealth() {
        return universityOverviewAppService.getSystemHealth();
    }
}
