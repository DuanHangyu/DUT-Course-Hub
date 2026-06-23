package com.human.digital.digitalhuman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.repository.mapper.*;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.repository.service.StudentCourseNodeStudyRecordService;
import com.human.digital.digitalhuman.service.model.dto.*;
import com.human.digital.digitalhuman.service.model.query.OverviewQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据概览服务类
 *
 * @Author taoHouChao
 * @Date 10:35 2026/3/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OverviewAppService {

    private final OperateLogMapper operateLogMapper;
    private final StudentMapper studentMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkSubmitMapper homeworkSubmitMapper;
    private final StudentCourseNodeStudyRecordMapper studyRecordMapper;
    private final CourseMapper courseMapper;
    private final CourseNodeMapper courseNodeMapper;
    private final CourseStudentRelationMapper courseStudentRelationMapper;
    private final StudentCourseNodeStudyRecordService studentCourseNodeStudyRecordService;
    private final StudentQuestionAnswerRecordMapper studentQuestionAnswerRecordMapper;

    /** 风险预警班级完成率阈值（低于此值视为风险班级） */
    private static final BigDecimal RISK_COMPLETION_RATE_THRESHOLD = BigDecimal.valueOf(40);

    /** 完成率计算系数 */
    private static final BigDecimal COMPLETION_RATE_MULTIPLIER = BigDecimal.valueOf(10);

    /** 完成率上限 */
    private static final BigDecimal COMPLETION_RATE_MAX = BigDecimal.valueOf(100);

    /**
     * 获取概览摘要数据
     *
     * @param query 查询参数
     * @return 概览摘要
     */
    public OverviewSummaryDTO getSummary(OverviewQuery query) {
        Integer schoolId = query.getSchoolId();

        OverviewSummaryDTO summary = new OverviewSummaryDTO();

        // 1. 实时活跃人数（最近24小时有操作记录的不同用户数）
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        List<OperateLogPO> activeLogs = operateLogMapper.selectList(
                new LambdaQueryWrapper<OperateLogPO>()
                        .select(OperateLogPO::getUserId)
                        .ge(OperateLogPO::getOperateTime, yesterday)
        );
        // 统计不同用户数
        long activeCount = activeLogs.stream()
                .map(OperateLogPO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        summary.setActiveCount((int) activeCount);

        // 2. 作业平均提交率
        BigDecimal homeworkRate = calculateHomeworkRate(schoolId);
        summary.setHomeworkRate(homeworkRate);

        // 3. AI响应数（最近7天，从问答记录表查询）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Long aiResponseCount = studentQuestionAnswerRecordMapper.selectCount(
                new LambdaQueryWrapper<StudentQuestionAnswerRecordPO>()
                        .ge(StudentQuestionAnswerRecordPO::getCreateTime, weekAgo)
        );
        summary.setAiResponseCount(aiResponseCount);

        // 4. 风险预警班级数
        Integer riskClassCount = calculateRiskClassCount(schoolId);
        summary.setRiskClassCount(riskClassCount);

        return summary;
    }

    /**
     * 计算作业提交率
     * 单个作业提交率 = 提交该作业的学生数 / 课程绑定的学生总数
     * 最终提交率 = 所有作业提交率的平均值
     *
     * @param schoolId 学校ID
     * @return 作业提交率
     */
    private BigDecimal calculateHomeworkRate(Integer schoolId) {
        List<CoursePO> coursePOS = courseMapper.selectList(
                Wrappers.lambdaQuery(CoursePO.class)
                        .select(CoursePO::getId)
                        .eq(CoursePO::getSchoolId, schoolId));
        if (CollectionUtils.isEmpty(coursePOS)) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        List<Integer> courseIds = coursePOS.stream().map(CoursePO::getId).toList();
        List<HomeworkPO> homeworkList = homeworkMapper.selectList(
                new LambdaQueryWrapper<HomeworkPO>()
                        .in(HomeworkPO::getCourseId, courseIds));

        if (CollectionUtils.isEmpty(homeworkList)) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        // 批量查询所有课程的选课关系，获取每个课程绑定的学生总数
        List<CourseStudentRelationPO> allRelations = courseStudentRelationMapper.selectList(
                new LambdaQueryWrapper<CourseStudentRelationPO>()
                        .in(CourseStudentRelationPO::getCourseId, courseIds));

        // 按课程ID分组统计学生数
        Map<Integer, Long> courseStudentCountMap = allRelations.stream()
                .collect(Collectors.groupingBy(
                        CourseStudentRelationPO::getCourseId,
                        Collectors.counting()));

        // 批量查询所有作业的提交记录
        List<Integer> homeworkIds = homeworkList.stream().map(HomeworkPO::getId).toList();
        List<HomeworkSubmitPO> allSubmits = homeworkSubmitMapper.selectList(
                new LambdaQueryWrapper<HomeworkSubmitPO>()
                        .in(HomeworkSubmitPO::getHomeworkId, homeworkIds));

        // 按作业ID分组统计提交学生数
        Map<Integer, Long> homeworkSubmitCountMap = allSubmits.stream()
                .collect(Collectors.groupingBy(
                        HomeworkSubmitPO::getHomeworkId,
                        Collectors.counting()));

        // 计算每个作业的提交率并累加
        BigDecimal totalSubmitRate = BigDecimal.ZERO;
        int validHomeworkCount = 0;

        for (HomeworkPO homework : homeworkList) {
            Integer courseId = homework.getCourseId();
            Long studentCount = courseStudentCountMap.get(courseId);
            // 跳过没有绑定学生的课程
            if (studentCount == null || studentCount == 0) {
                continue;
            }

            Long submitCount = homeworkSubmitCountMap.getOrDefault(homework.getId(), 0L);
            BigDecimal submitRate = BigDecimal.valueOf(submitCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
            totalSubmitRate = totalSubmitRate.add(submitRate);
            validHomeworkCount++;
        }

        if (validHomeworkCount == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        // 计算平均提交率
        return totalSubmitRate.divide(BigDecimal.valueOf(validHomeworkCount), 1, RoundingMode.HALF_UP);
    }

    /**
     * 计算风险预警班级数
     *
     * @param schoolId 学校ID
     * @return 风险预警班级数
     */
    private Integer calculateRiskClassCount(Integer schoolId) {
        // 1. 批量获取班级列表
        List<SchoolClassPO> classes = schoolClassMapper.selectList(
                new LambdaQueryWrapper<SchoolClassPO>()
                        .eq(schoolId != null, SchoolClassPO::getSchoolId, schoolId)
        );

        if (classes.isEmpty()) {
            return 0;
        }

        // 2. 批量获取所有班级的学生（避免N+1查询）
        List<Integer> classIds = classes.stream().map(SchoolClassPO::getId).toList();
        List<StudentPO> allStudents = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPO>()
                        .in(StudentPO::getClassId, classIds)
        );

        if (allStudents.isEmpty()) {
            return 0;
        }

        // 3. 按班级ID分组学生
        Map<Integer, List<StudentPO>> studentsByClassId = allStudents.stream()
                .collect(Collectors.groupingBy(StudentPO::getClassId));

        // 4. 批量获取所有学生的学习记录（避免N+1查询）
        List<Integer> studentIds = allStudents.stream().map(StudentPO::getId).toList();
        List<StudentCourseNodeStudyRecordPO> allRecords = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudentCourseNodeStudyRecordPO>()
                        .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
        );

        // 5. 按学生ID分组学习记录
        Map<Integer, List<StudentCourseNodeStudyRecordPO>> recordsByStudentId = allRecords.stream()
                .collect(Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId));

        // 6. 在内存中计算每个班级的平均完成率
        int riskCount = 0;
        for (SchoolClassPO schoolClass : classes) {
            List<StudentPO> students = studentsByClassId.getOrDefault(schoolClass.getId(), List.of());

            if (students.isEmpty()) {
                continue;
            }

            BigDecimal totalRate = BigDecimal.ZERO;
            for (StudentPO student : students) {
                List<StudentCourseNodeStudyRecordPO> records =
                        recordsByStudentId.getOrDefault(student.getId(), List.of());
                totalRate = totalRate.add(calculateStudentCompletionRate(records));
            }

            BigDecimal avgRate = totalRate.divide(BigDecimal.valueOf(students.size()), 2, RoundingMode.HALF_UP);

            // 完成率低于40%为风险班级
            if (avgRate.compareTo(RISK_COMPLETION_RATE_THRESHOLD) < 0) {
                riskCount++;
            }
        }

        return riskCount;
    }

    /**
     * 计算学生课程完成率
     *
     * @param studentId 学生ID
     * @return 完成率
     */
    private BigDecimal calculateStudentCompletionRate(Integer studentId) {
        // 统计学生的学习记录
        List<StudentCourseNodeStudyRecordPO> records = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudentCourseNodeStudyRecordPO>()
                        .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
        );

        return calculateStudentCompletionRate(records);
    }

    /**
     * 根据学习记录计算学生课程完成率
     *
     * @param records 学习记录列表
     * @return 完成率
     */
    private BigDecimal calculateStudentCompletionRate(List<StudentCourseNodeStudyRecordPO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return BigDecimal.ZERO;
        }

        // 简单计算：完成的学习记录数/总记录数 * 100
        // 这里简化为返回记录数作为完成率（实际应根据课程节点总数计算）
        return COMPLETION_RATE_MAX.min(
                BigDecimal.valueOf(records.size()).multiply(COMPLETION_RATE_MULTIPLIER)
        );
    }

    /**
     * 获取24小时活跃趋势
     *
     * @param schoolId 学校ID
     * @return 趋势数据
     */
    public TrendDataDTO getActiveTrend24h(Integer schoolId) {
        TrendDataDTO trendData = new TrendDataDTO();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        // 获取最近24小时的数据
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hourStart = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);

            // 统计该小时内的活跃用户数
            Long count = operateLogMapper.selectCount(
                    new LambdaQueryWrapper<OperateLogPO>()
                            .between(OperateLogPO::getOperateTime, hourStart, hourEnd)
            );

            int hour = now.minusHours(i).getHour();
            dates.add(String.format("%02d:00", hour));
            values.add(count.intValue());
        }

        trendData.setDates(dates);
        trendData.setValues(values);
        return trendData;
    }

    /**
     * 获取作业提交趋势（近7天）
     *
     * @param schoolId 学校ID
     * @return 趋势数据
     */
    public TrendDataDTO getHomeworkTrend7d(Integer schoolId) {
        TrendDataDTO trendData = new TrendDataDTO();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 统计当天的作业提交率
            BigDecimal rate = calculateDailyHomeworkRate(schoolId, dayStart, dayEnd);

            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            values.add(rate.setScale(1, RoundingMode.HALF_UP).intValue());
        }

        trendData.setDates(dates);
        trendData.setValues(values);
        return trendData;
    }

    /**
     * 计算每日作业提交率
     *
     * @param schoolId  学校ID
     * @param dayStart  开始时间
     * @param dayEnd    结束时间
     * @return 提交率
     */
    private BigDecimal calculateDailyHomeworkRate(Integer schoolId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        // 获取当天布置的作业
        List<HomeworkPO> homeworkList = homeworkMapper.selectList(
                new LambdaQueryWrapper<HomeworkPO>()
                        .between(HomeworkPO::getCreateTime, dayStart, dayEnd)
        );

        if (homeworkList.isEmpty()) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        int totalHomework = homeworkList.size();
        int totalSubmitted = 0;

        for (HomeworkPO homework : homeworkList) {
            Long submittedCount = homeworkSubmitMapper.selectCount(
                    new LambdaQueryWrapper<HomeworkSubmitPO>()
                            .eq(HomeworkSubmitPO::getHomeworkId, homework.getId())
                            .between(HomeworkSubmitPO::getSubmitTime, dayStart, dayEnd)
            );
            if (submittedCount > 0) {
                totalSubmitted++;
            }
        }

        if (totalHomework == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(totalSubmitted)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalHomework), 1, RoundingMode.HALF_UP);
    }

    /**
     * 获取AI响应趋势（近7天）
     *
     * @param schoolId 学校ID
     * @return 趋势数据
     */
    public TrendDataDTO getAiResponseTrend7d(Integer schoolId) {
        TrendDataDTO trendData = new TrendDataDTO();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            Long count = operateLogMapper.selectCount(
                    new LambdaQueryWrapper<OperateLogPO>()
                            .between(OperateLogPO::getOperateTime, dayStart, dayEnd)
                            .and(w -> w.like(OperateLogPO::getOperation, "AI")
                                    .or().like(OperateLogPO::getOperation, "智能"))
            );

            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            values.add(count.intValue());
        }

        trendData.setDates(dates);
        trendData.setValues(values);
        return trendData;
    }

    /**
     * 获取风险预警趋势（近10天）
     *
     * @param schoolId 学校ID
     * @return 趋势数据
     */
    public TrendDataDTO getRiskTrend10d(Integer schoolId) {
        TrendDataDTO trendData = new TrendDataDTO();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 9; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            // 统计当天的风险预警班级数
            Integer count = calculateDailyRiskClassCount(schoolId, date);

            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            values.add(count);
        }

        trendData.setDates(dates);
        trendData.setValues(values);
        return trendData;
    }

    /**
     * 计算每日风险班级数
     *
     * @param schoolId 学校ID
     * @param date     日期
     * @return 风险班级数
     */
    private Integer calculateDailyRiskClassCount(Integer schoolId, LocalDate date) {
        // 1. 批量获取班级列表
        List<SchoolClassPO> classes = schoolClassMapper.selectList(
                new LambdaQueryWrapper<SchoolClassPO>()
                        .eq(schoolId != null, SchoolClassPO::getSchoolId, schoolId)
        );

        if (classes.isEmpty()) {
            return 0;
        }

        // 2. 批量获取所有班级的学生（避免N+1查询）
        List<Integer> classIds = classes.stream().map(SchoolClassPO::getId).toList();
        List<StudentPO> allStudents = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPO>()
                        .in(StudentPO::getClassId, classIds)
        );

        if (allStudents.isEmpty()) {
            return 0;
        }

        // 3. 按班级ID分组学生
        Map<Integer, List<StudentPO>> studentsByClassId = allStudents.stream()
                .collect(Collectors.groupingBy(StudentPO::getClassId));

        // 4. 批量获取指定日期范围内的所有学习记录（避免N+1查询）
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Integer> studentIds = allStudents.stream().map(StudentPO::getId).toList();
        List<StudentCourseNodeStudyRecordPO> allRecords = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudentCourseNodeStudyRecordPO>()
                        .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
                        .between(StudentCourseNodeStudyRecordPO::getStudyStartTime, dayStart, dayEnd)
        );

        // 5. 按学生ID分组学习记录
        Map<Integer, List<StudentCourseNodeStudyRecordPO>> recordsByStudentId = allRecords.stream()
                .collect(Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId));

        // 6. 在内存中计算每个班级的平均完成率
        int riskCount = 0;
        for (SchoolClassPO schoolClass : classes) {
            List<StudentPO> students = studentsByClassId.getOrDefault(schoolClass.getId(), List.of());

            if (students.isEmpty()) {
                continue;
            }

            BigDecimal totalRate = BigDecimal.ZERO;
            for (StudentPO student : students) {
                List<StudentCourseNodeStudyRecordPO> records =
                        recordsByStudentId.getOrDefault(student.getId(), List.of());
                totalRate = totalRate.add(calculateStudentCompletionRate(records));
            }

            BigDecimal avgRate = totalRate.divide(BigDecimal.valueOf(students.size()), 2, RoundingMode.HALF_UP);

            if (avgRate.compareTo(RISK_COMPLETION_RATE_THRESHOLD) < 0) {
                riskCount++;
            }
        }

        return riskCount;
    }

    /**
     * 获取学习时间热力图
     *
     * @param query 查询参数
     * @return 热力图数据
     */
    public HeatmapDTO getHeatmap(OverviewQuery query) {
        HeatmapDTO heatmap = new HeatmapDTO();
        Integer schoolId = query.getSchoolId();

        // 获取学生列表
        List<StudentPO> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPO>()
                        .eq(schoolId != null, StudentPO::getSchoolId, schoolId)
        );

        // 随机取学生
        if (CollectionUtils.isNotEmpty(students) && students.size() > 10) {
            Collections.shuffle(students);
            students = students.subList(0, 10);
        }

        List<String> studentNames = students.stream()
                .map(StudentPO::getStudentName)
                .collect(Collectors.toList());

        // 获取日期范围（默认近7天）
        LocalDate endDate = query.getEndDate() != null
                ? LocalDate.parse(query.getEndDate())
                : LocalDate.now();
        LocalDate startDate = query.getStartDate() != null
                ? LocalDate.parse(query.getStartDate())
                : endDate.minusDays(6);

        List<String> dates = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= daysBetween; i++) {
            dates.add(startDate.plusDays(i).format(DateTimeFormatter.ofPattern("MM-dd")));
        }

        // 构建热力值矩阵
        List<List<Integer>> values = new ArrayList<>();
        for (StudentPO student : students) {
            List<Integer> studentValues = new ArrayList<>();
            for (int i = 0; i <= daysBetween; i++) {
                LocalDate date = startDate.plusDays(i);
                // 统计该学生的学习时长
                Integer studyTime = calculateDailyStudyTime(student.getId(), date);
                studentValues.add(studyTime);
            }
            values.add(studentValues);
        }

        heatmap.setStudents(studentNames);
        heatmap.setDates(dates);
        heatmap.setValues(values);
        return heatmap;
    }

    /**
     * 计算每日学习时长
     *
     * @param studentId 学生ID
     * @param date     日期
     * @return 学习时长（分钟）
     */
    private Integer calculateDailyStudyTime(Integer studentId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        // 统计该学生的学习时长（分钟）
        List<StudentCourseNodeStudyRecordPO> records = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudentCourseNodeStudyRecordPO>()
                        .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                        .between(StudentCourseNodeStudyRecordPO::getStudyStartTime, dayStart, dayEnd)
        );

        int totalMinutes = 0;
        for (StudentCourseNodeStudyRecordPO record : records) {
            totalMinutes += record.getStudyTime();
        }
        return totalMinutes;
    }

    /**
     * 获取班级学习进度
     *
     * @param schoolId 学校ID
     * @return 班级进度列表
     */
    public List<ClassProgressDTO> getClassProgress(Integer schoolId) {
        // 1. 批量获取班级列表
        List<SchoolClassPO> classes = schoolClassMapper.selectList(
                new LambdaQueryWrapper<SchoolClassPO>()
                        .eq(schoolId != null, SchoolClassPO::getSchoolId, schoolId)
        );

        if (classes.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量获取所有班级的学生
        List<Integer> classIds = classes.stream().map(SchoolClassPO::getId).toList();
        List<StudentPO> allStudents = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPO>()
                        .in(StudentPO::getClassId, classIds)
        );

        // 3. 按班级ID分组学生
        Map<Integer, List<StudentPO>> classStudentsMap = allStudents.stream()
                .collect(Collectors.groupingBy(StudentPO::getClassId));

        // 4. 批量获取所有学生的学习记录
        List<Integer> studentIds = allStudents.stream().map(StudentPO::getId).toList();
        List<StudentCourseNodeStudyRecordPO> allRecords = studentIds.isEmpty() ? new ArrayList<>() :
                studyRecordMapper.selectList(
                        new LambdaQueryWrapper<StudentCourseNodeStudyRecordPO>()
                                .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
                );

        // 5. 按学生ID分组学习记录
        Map<Integer, List<StudentCourseNodeStudyRecordPO>> studentRecordsMap = allRecords.stream()
                .collect(Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId));

        // 6. 计算每个班级的平均完成率
        List<ClassProgressDTO> result = new ArrayList<>();
        for (SchoolClassPO schoolClass : classes) {
            List<StudentPO> students = classStudentsMap.getOrDefault(schoolClass.getId(), new ArrayList<>());

            if (students.isEmpty()) {
                continue;
            }

            // 计算班级平均完成率
            BigDecimal totalRate = BigDecimal.ZERO;
            for (StudentPO student : students) {
                List<StudentCourseNodeStudyRecordPO> records =
                        studentRecordsMap.getOrDefault(student.getId(), new ArrayList<>());
                totalRate = totalRate.add(calculateStudentCompletionRate(records));
            }

            BigDecimal avgRate = totalRate.divide(BigDecimal.valueOf(students.size()), 1, RoundingMode.HALF_UP);

            result.add(new ClassProgressDTO(
                    schoolClass.getId(),
                    schoolClass.getClassName(),
                    avgRate
            ));
        }

        // 7. 按完成率降序排列
        result.sort(Comparator.comparingDouble(ClassProgressDTO::completionRateDouble).reversed());

        return result;
    }

    /**
     * 获取活跃度趋势（近30天）
     *
     * @param schoolId 学校ID
     * @return 趋势数据
     */
    public TrendDataDTO getTrend(Integer schoolId) {
        TrendDataDTO trendData = new TrendDataDTO();
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 统计当天的活跃用户数
            Long count = operateLogMapper.selectCount(
                    new LambdaQueryWrapper<OperateLogPO>()
                            .between(OperateLogPO::getOperateTime, dayStart, dayEnd)
            );

            // 计算活跃度百分比
            Long totalStudents = studentMapper.selectCount(
                    new LambdaQueryWrapper<StudentPO>()
                            .eq(schoolId != null, StudentPO::getSchoolId, schoolId)
            );

            int percentage = totalStudents > 0 ? (int) (count * 100.0 / totalStudents) : 0;

            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            values.add(percentage);
        }

        trendData.setDates(dates);
        trendData.setValues(values);
        return trendData;
    }

    /**
     * 获取热门课程（Top3）
     *
     * @param schoolId 学校ID
     * @return 热门课程列表
     */
    public List<PopularCourseDTO> getPopularCourses(Integer schoolId) {
        // 1. 获取学校所有课程
        List<CoursePO> courses = courseMapper.selectList(
                new LambdaQueryWrapper<CoursePO>()
                        .eq(schoolId != null, CoursePO::getSchoolId, schoolId)
        );

        if (courses.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量查询所有课程的选课关系
        List<Integer> courseIds = courses.stream().map(CoursePO::getId).collect(Collectors.toList());
        List<CourseStudentRelationPO> allRelations = courseStudentRelationMapper.selectList(
                new LambdaQueryWrapper<CourseStudentRelationPO>()
                        .in(CourseStudentRelationPO::getCourseId, courseIds)
        );

        // 3. 按课程ID分组统计选课人数
        Map<Integer, Long> courseStudentCountMap = allRelations.stream()
                .collect(Collectors.groupingBy(
                        CourseStudentRelationPO::getCourseId,
                        Collectors.counting()
                ));

        // 4. 构建课程热门度列表
        List<PopularCourseDTO> result = new ArrayList<>();
        for (CoursePO course : courses) {
            Long studentCount = courseStudentCountMap.getOrDefault(course.getId(), 0L);
            result.add(new PopularCourseDTO(
                    course.getId().longValue(),
                    course.getCourseName(),
                    studentCount.intValue()
            ));
        }

        // 5. 按选课人数降序排列，取前3
        return result.stream()
                .sorted((a, b) -> Integer.compare(b.getStudentCount(), a.getStudentCount()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 获取课程完成度
     * 返回学校所有课程的完成度列表
     *
     * @param schoolId 学校ID
     * @return 课程完成度
     */
    public List<CourseCompletionItemDTO> getCourseCompletion(Integer schoolId) {
        // 通过一条聚合SQL直接在数据库层面计算完成度，避免大量数据加载到内存
        List<CourseCompletionItemDTO> result = courseMapper.selectCourseCompletionStats(schoolId);

        // 计算完成率
        for (CourseCompletionItemDTO item : result) {
            if (item.getTotalCount() == null || item.getTotalCount() == 0) {
                item.setCompletionRate(BigDecimal.ZERO);
                item.setCompletedCount(0);
                item.setUncompletedCount(0);
                item.setTotalCount(0);
            } else {
                BigDecimal rate = BigDecimal.valueOf(item.getCompletedCount())
                        .divide(BigDecimal.valueOf(item.getTotalCount()), 1, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                item.setCompletionRate(rate);
            }
        }

        return result;
    }
}
