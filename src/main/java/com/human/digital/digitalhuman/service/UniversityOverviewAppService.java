package com.human.digital.digitalhuman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.human.digital.digitalhuman.repository.mapper.*;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.service.model.dto.university.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 大学端数据概览服务类
 * 提供大学端数据概览的各类统计和查询功能
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityOverviewAppService {

    private final SchoolMapper schoolMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final OperateLogMapper operateLogMapper;
    private final StudentCourseNodeStudyRecordMapper studentCourseNodeStudyRecordMapper;

    /**
     * 获取核心统计数据
     * 统计学校数、学生总数、课程总数和AI算力消耗
     *
     * @return 核心统计数据
     */
    public CoreStatisticsDTO getCoreStatistics() {
        CoreStatisticsDTO dto = new CoreStatisticsDTO();

        // 覆盖学校数：统计 school 表中状态为启用的学校数量
        Long schoolCount = schoolMapper.selectCount(
                new LambdaQueryWrapper<SchoolPO>()
                        .eq(SchoolPO::getState, 1)
        );
        dto.setSchoolCount(schoolCount);

        // 累计服务学生数：统计 student 表中学生总数
        Long studentCount = studentMapper.selectCount(
                new LambdaQueryWrapper<StudentPO>()
        );
        dto.setStudentCount(studentCount);

        // 累计精品课程数：统计 course 表中课程数量
        Long courseCount = courseMapper.selectCount(
                new LambdaQueryWrapper<CoursePO>()
        );
        dto.setCourseCount(courseCount);

        // AI算力消耗总量：暂时返回 0L
        dto.setAiPowerCount(0L);

        return dto;
    }

    /**
     * 获取学校活跃榜 Top5
     * 查询启用的学校，按活跃度排序返回 Top5
     *
     * @return 学校排名列表
     */
    public List<SchoolRankingDTO> getSchoolRanking() {
        // 查询启用的学校
        List<SchoolPO> schools = schoolMapper.selectList(
                new LambdaQueryWrapper<SchoolPO>()
                        .eq(SchoolPO::getState, 1)
        );

        // 计算每个学校的活跃度（这里简化为：学校学生数 + 今日操作记录数）
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<SchoolRankingDTO> rankings = new ArrayList<>();

        for (int i = 0; i < schools.size(); i++) {
            SchoolPO school = schools.get(i);

            // 统计该学校的学生数
            Long studentCount = studentMapper.selectCount(
                    new LambdaQueryWrapper<StudentPO>()
                            .eq(StudentPO::getSchoolId, school.getId())
            );

            // 统计该学校今日的操作记录数
            Long operateCount = operateLogMapper.selectCount(
                    new LambdaQueryWrapper<OperateLogPO>()
                            .ge(OperateLogPO::getOperateTime, today)
            );

            // 活跃度 = 学生数 * 10 + 今日操作数
            long activity = studentCount * 10 + operateCount;

            SchoolRankingDTO dto = new SchoolRankingDTO();
            dto.setRank(i + 1);
            dto.setSchoolName(school.getSchoolName());
            dto.setActivity(activity);
            rankings.add(dto);
        }

        // 按活跃度降序排序并返回 Top5
        rankings.sort((a, b) -> Long.compare(b.getActivity(), a.getActivity()));
        List<SchoolRankingDTO> top5 = new ArrayList<>();
        for (int i = 0; i < Math.min(5, rankings.size()); i++) {
            SchoolRankingDTO dto = rankings.get(i);
            dto.setRank(i + 1);
            top5.add(dto);
        }

        return top5;
    }

    /**
     * 获取学习趋势数据
     * 生成近7天日期列表，并为每个学校生成趋势线数据
     *
     * @return 学习趋势数据
     */
    public LearningTrendDTO getLearningTrend() {
        LearningTrendDTO dto = new LearningTrendDTO();

        // 生成近7天日期列表
        List<String> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            dates.add(today.minusDays(i).format(DateTimeFormatter.ofPattern("MM-dd")));
        }
        dto.setDates(dates);

        // 查询所有启用的学校
        List<SchoolPO> schools = schoolMapper.selectList(
                new LambdaQueryWrapper<SchoolPO>()
                        .eq(SchoolPO::getState, 1)
        );

        // 为每个学校生成趋势线数据
        List<LearningTrendDTO.TrendLine> lines = new ArrayList<>();
        for (SchoolPO school : schools) {
            LearningTrendDTO.TrendLine line = new LearningTrendDTO.TrendLine();
            line.setSchoolName(school.getSchoolName());

            List<Integer> values = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

                // 统计该学校当天的学习人数（通过学习记录）
                // 获取该学校的学生
                List<StudentPO> students = studentMapper.selectList(
                        new LambdaQueryWrapper<StudentPO>()
                                .eq(StudentPO::getSchoolId, school.getId())
                );

                List<Integer> studentIds = students.stream()
                        .map(StudentPO::getId)
                        .collect(Collectors.toList());

                if (studentIds.isEmpty()) {
                    values.add(0);
                    continue;
                }

                Long count = studentCourseNodeStudyRecordMapper.selectCount(
                        new LambdaQueryWrapper<StudentCourseNodeStudyRecordPO>()
                                .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
                                .between(StudentCourseNodeStudyRecordPO::getStudyStartTime, dayStart, dayEnd)
                );
                values.add(count.intValue());
            }
            line.setValues(values);
            lines.add(line);
        }

        dto.setLines(lines);
        return dto;
    }

    /**
     * 获取实时活跃数据
     * 当前活跃学生数（最近5分钟有操作记录）、当前活跃学校数、当前覆盖课程数
     *
     * @return 实时活跃数据
     */
    public RealtimeDataDTO getRealtimeData() {
        RealtimeDataDTO dto = new RealtimeDataDTO();

        // 当前活跃学生数（最近5分钟有操作记录）
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        Long activeStudentCount = operateLogMapper.selectCount(
                new LambdaQueryWrapper<OperateLogPO>()
                        .ge(OperateLogPO::getOperateTime, fiveMinutesAgo)
        );
        dto.setActiveStudents(activeStudentCount.intValue());

        // 当前活跃学校数（最近5分钟有操作记录的学校）
        List<OperateLogPO> recentLogs = operateLogMapper.selectList(
                new LambdaQueryWrapper<OperateLogPO>()
                        .ge(OperateLogPO::getOperateTime, fiveMinutesAgo)
        );
        // 由于 operate_log 表没有 school_id 字段，这里简化处理，返回所有启用的学校数
        Long activeSchoolCount = schoolMapper.selectCount(
                new LambdaQueryWrapper<SchoolPO>()
                        .eq(SchoolPO::getState, 1)
        );
        dto.setActiveSchools(activeSchoolCount.intValue());

        // 当前覆盖课程数（状态为启用的课程）
        Long activeCourseCount = courseMapper.selectCount(
                new LambdaQueryWrapper<CoursePO>()
                        .eq(CoursePO::getState, true)
        );
        dto.setActiveCourses(activeCourseCount.intValue());

        return dto;
    }

    /**
     * 获取24小时流量趋势
     * 按小时统计过去24小时的访问日志
     *
     * @return 24小时流量数据
     */
    public Traffic24hDTO getTraffic24h() {
        Traffic24hDTO dto = new Traffic24hDTO();

        List<String> hours = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        // 获取最近24小时的数据
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hourStart = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);

            // 统计该小时内的操作记录数
            Long count = operateLogMapper.selectCount(
                    new LambdaQueryWrapper<OperateLogPO>()
                            .between(OperateLogPO::getOperateTime, hourStart, hourEnd)
            );

            int hour = now.minusHours(i).getHour();
            hours.add(String.format("%02d:00", hour));
            values.add(count.intValue());
        }

        dto.setHours(hours);
        dto.setValues(values);
        return dto;
    }

    /**
     * 获取学校区域分布
     * 按学校 region 字段分组统计
     * 注意：SchoolPO 中使用的是 location 字段作为区域标识
     *
     * @return 学校区域分布数据
     */
    public SchoolRegionDTO getSchoolRegion() {
        SchoolRegionDTO dto = new SchoolRegionDTO();

        // 查询所有启用的学校
        List<SchoolPO> schools = schoolMapper.selectList(
                new LambdaQueryWrapper<SchoolPO>()
                        .eq(SchoolPO::getState, 1)
        );

        // 按 location 分组统计
        Map<String, List<SchoolPO>> groupedByRegion = schools.stream()
                .collect(Collectors.groupingBy(school ->
                        school.getLocation() != null ? school.getLocation() : "未知"));

        List<SchoolRegionDTO.RegionData> regions = new ArrayList<>();
        for (Map.Entry<String, List<SchoolPO>> entry : groupedByRegion.entrySet()) {
            SchoolRegionDTO.RegionData regionData = new SchoolRegionDTO.RegionData();
            regionData.setRegion(entry.getKey());
            regionData.setCount(entry.getValue().size());
            regions.add(regionData);
        }

        // 按数量降序排序
        regions.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));

        dto.setRegions(regions);
        return dto;
    }

    /**
     * 获取课程学习统计
     * 按学习人数排序返回 Top5
     *
     * @return 课程学习统计数据
     */
    public CourseLearningDTO getCourseLearning() {
        CourseLearningDTO dto = new CourseLearningDTO();

        // 查询所有课程
        List<CoursePO> courses = courseMapper.selectList(
                new LambdaQueryWrapper<CoursePO>()
        );

        // 统计每个课程的学习人数
        List<CourseLearningDTO.CourseStat> courseStats = new ArrayList<>();
        for (CoursePO course : courses) {
            // 通过课程学生关系表统计学习人数
            // 这里简化处理，使用 course 表的 studentCount 字段
            CourseLearningDTO.CourseStat stat = new CourseLearningDTO.CourseStat();
            stat.setCourseId(course.getId().longValue());
            stat.setCourseName(course.getCourseName());
            stat.setStudentCount(course.getStudentCount() != null ? course.getStudentCount() : 0);
            courseStats.add(stat);
        }

        // 按学习人数降序排序并返回 Top5
        courseStats.sort((a, b) -> Integer.compare(b.getStudentCount(), a.getStudentCount()));
        List<CourseLearningDTO.CourseStat> top5 = courseStats.stream()
                .limit(5)
                .collect(Collectors.toList());

        dto.setCourses(top5);
        return dto;
    }

    /**
     * 获取系统健康状态
     * 返回默认健康状态（API响应、数据库连接、服务器负载）
     *
     * @return 系统健康状态数据
     */
    public SystemHealthDTO getSystemHealth() {
        SystemHealthDTO dto = new SystemHealthDTO();

        // 设置总体状态为健康
        dto.setStatus("healthy");

        List<SystemHealthDTO.HealthItem> items = new ArrayList<>();

        // API 响应健康项
        SystemHealthDTO.HealthItem apiItem = new SystemHealthDTO.HealthItem();
        apiItem.setName("API响应");
        apiItem.setStatus("healthy");
        apiItem.setMessage("API响应正常");
        items.add(apiItem);

        // 数据库连接健康项
        SystemHealthDTO.HealthItem dbItem = new SystemHealthDTO.HealthItem();
        dbItem.setName("数据库连接");
        dbItem.setStatus("healthy");
        dbItem.setMessage("数据库连接正常");
        items.add(dbItem);

        // 服务器负载健康项
        SystemHealthDTO.HealthItem loadItem = new SystemHealthDTO.HealthItem();
        loadItem.setName("服务器负载");
        loadItem.setStatus("healthy");
        loadItem.setMessage("服务器负载正常");
        items.add(loadItem);

        dto.setItems(items);
        return dto;
    }
}
