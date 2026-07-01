package com.human.digital.digitalhuman.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.repository.service.*;
import com.human.digital.digitalhuman.service.StudyMonitorAppService;
import com.human.digital.digitalhuman.service.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 学情监控应用服务实现类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyMonitorAppServiceImpl implements StudyMonitorAppService {

    // ========== 常量定义 ==========
    private static final String STUDENT_NAME_PREFIX = "学生";
    private static final String RISK_HIGH = "high";
    private static final String RISK_MEDIUM = "medium";
    private static final String RISK_LOW = "low";

    // 百分比乘数
    private static final int PERCENTAGE_MULTIPLIER = 100;
    // 完成率小数位数
    private static final int COMPLETION_RATE_SCALE = 2;

    // 风险等级阈值（小时）
    private static final long STUCK_HIGH_RISK_HOURS = 24 * 7L;  // > 7天
    private static final long STUCK_MEDIUM_RISK_HOURS = 24 * 3L; // 3-7天
    private static final long STUCK_LOW_RISK_HOURS = 24L;         // 1-3天

    // 进度滞后阈值（百分比）
    private static final int LAGGING_HIGH_THRESHOLD = 50;
    private static final int LAGGING_MEDIUM_THRESHOLD = 70;
    private static final int LAGGING_LOW_THRESHOLD = 80;

    private final UserService userService;
    private final CourseService courseService;
    private final CourseNodeService courseNodeService;
    private final CourseStudentRelationService courseStudentRelationService;
    private final StudentCourseNodeStudyRecordService studentCourseNodeStudyRecordService;
    private final StudyProgressSnapshotService studyProgressSnapshotService;
    private final StudyRiskWarningService studyRiskWarningService;
    private final KnowledgeBlindSpotService knowledgeBlindSpotService;
    private final StudentQuestionAnswerRecordService studentQuestionAnswerRecordService;
    private final StudentService studentService;
    private final HomeworkService homeworkService;
    private final HomeworkSubmitService homeworkSubmitService;
    private final SchoolClassService schoolClassService;

    // ========== 公共查询方法 ==========

    /**
     * 获取课程关联的学生ID列表
     */
    private List<Long> getCourseStudentIds(Long courseId) {
        List<CourseStudentRelationPO> relations = courseStudentRelationService.queryByCourseIds(List.of(courseId.intValue()));
        return relations.stream()
                .map(r -> r.getStudentId().longValue())
                .distinct()
                .toList();
    }

    /**
     * 获取课程节点列表
     */
    private List<CourseNodePO> getCourseNodes(Long courseId) {
        return courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .eq(CourseNodePO::getCourseId, courseId.intValue()));
    }

    /**
     * 判断节点是否已完成（有学习记录的completed字段为true）
     */
    private boolean isNodeCompleted(Integer nodeId, Integer studentId) {
        return studentCourseNodeStudyRecordService.hasCompletedRecord(studentId, nodeId);
    }

    /**
     * 根据节点完成状态将学生分组（批量查询优化）
     *
     * @param nodeId     节点ID
     * @param studentIds 学生ID列表
     * @return 包含已完成和未完成学生列表的封装对象
     */
    private StudentGroupByCompletion splitStudentsByNodeCompletion(Integer nodeId, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new StudentGroupByCompletion(List.of(), List.of());
        }

        // 批量查询已完成的学生ID，将 N+1 查询降为 1 次查询
        List<Integer> completedStudentIds = studentCourseNodeStudyRecordService
                .listCompletedStudentIds(nodeId, studentIds.stream().map(Long::intValue).toList());
        Set<Integer> completedSet = new HashSet<>(completedStudentIds);

        List<StudentSimpleDTO> completedStudents = new ArrayList<>();
        List<StudentSimpleDTO> incompleteStudents = new ArrayList<>();
        Map<Integer, StudentPO> studentIdMap = studentService.listByIds(studentIds).stream()
                .collect(Collectors.toMap(StudentPO::getId, Function.identity()));

        for (Long studentId : studentIds) {
            String studentName = Optional.ofNullable(studentIdMap.get(studentId.intValue())).map(StudentPO::getStudentName).orElse("");
            StudentSimpleDTO student = new StudentSimpleDTO(studentId, studentName, null);
            if (completedSet.contains(studentId.intValue())) {
                completedStudents.add(student);
            } else {
                incompleteStudents.add(student);
            }
        }
        return new StudentGroupByCompletion(completedStudents, incompleteStudents);
    }

    /**
     * 学生分组结果封装
     */
    private record StudentGroupByCompletion(List<StudentSimpleDTO> completedStudents,
                                           List<StudentSimpleDTO> incompleteStudents) {
    }

    // ========== 接口实现 ==========

    @Override
    public CoreIndicatorOverviewDTO getCoreIndicatorOverview(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<Long> studentIds = getCourseStudentIds(courseId);
        if (studentIds.isEmpty()) {
            return emptyOverview();
        }

        // 批量获取所有考核成绩
        Map<String, BigDecimal> progressMap = calculateAllStudentProgress(courseId, studentIds);

        // 计算统计数据
        BigDecimal avgProgress = progressMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(studentIds.size()), 2, RoundingMode.HALF_UP);

        long completedCount = progressMap.values().stream()
                .filter(p -> p.compareTo(BigDecimal.valueOf(100)) >= 0)
                .count();

        ProgressIndicatorDTO progress = new ProgressIndicatorDTO(avgProgress, BigDecimal.ZERO, studentIds.size(), (int) completedCount);
        ActivityIndicatorDTO activity = calculateActivity(courseId, studentIds);
        StuckIndicatorDTO stuck = calculateStuckIndicators(courseId, studentIds);
        LaggingIndicatorDTO lagging = calculateLaggingIndicators(progressMap, studentIds.size());

        return new CoreIndicatorOverviewDTO(progress, activity, stuck, lagging);
    }

    @Override
    public ProgressTrendDTO getProgressTrend(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<StudyProgressSnapshotPO> snapshots = studyProgressSnapshotService.listByCourseIdAndDate(courseId, LocalDate.now());

        List<TrendPointDTO> trends = snapshots.stream()
                .collect(Collectors.groupingBy(StudyProgressSnapshotPO::getSnapshotDate))
                .entrySet().stream()
                .map(entry -> {
                    BigDecimal avg = entry.getValue().stream()
                            .map(StudyProgressSnapshotPO::getProgress)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP);
                    return new TrendPointDTO(entry.getKey().toString(), avg);
                })
                .sorted(Comparator.comparing(TrendPointDTO::getDate))
                .toList();

        return new ProgressTrendDTO(trends);
    }

    /**
     * 获取课程知识图谱数据
     *
     * @param courseId   课程ID
     * @param teacherId  教师ID
     * @return 知识图谱DTO
     */
    @Override
    public KnowledgeGraphDTO getKnowledgeGraph(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<CourseNodePO> nodes = getCourseNodes(courseId);

        // 获取课程关联的学生ID列表
        List<Long> studentIds = getCourseStudentIds(courseId);
        final int totalStudents = studentIds.size();

        // 记录关键业务日志
        log.info("getKnowledgeGraph, courseId:{}, teacherId:{}, totalStudents:{}", courseId, teacherId, totalStudents);

        // 批量查询所有节点的学习记录
        Map<Integer, Long> nodeCompletedCountMap;
        if (totalStudents > 0) {
            List<Integer> nodeIds = nodes.stream().map(CourseNodePO::getId).collect(Collectors.toList());
            List<StudentCourseNodeStudyRecordPO> allStudyRecords = studentCourseNodeStudyRecordService.list(
                    Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                            .in(StudentCourseNodeStudyRecordPO::getCourseNodeId, nodeIds)
                            .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds.stream().map(Long::intValue).collect(Collectors.toList()))
            );

            // 按学生ID和节点ID分组，取最新的记录
            Map<String, StudentCourseNodeStudyRecordPO> latestRecordMap = new HashMap<>();
            for (StudentCourseNodeStudyRecordPO record : allStudyRecords) {
                String key = record.getStudentId() + "_" + record.getCourseNodeId();
                StudentCourseNodeStudyRecordPO existing = latestRecordMap.get(key);
                if (existing == null && Objects.equals(record.getCompleted(), Boolean.TRUE)) {
                    latestRecordMap.put(key, record);
                }
            }

            // 统计每个节点完成的学生数（学习记录的completed字段为true）
            nodeCompletedCountMap = new HashMap<>();
            for (StudentCourseNodeStudyRecordPO record : latestRecordMap.values()) {
                if (Boolean.TRUE.equals(record.getCompleted())) {
                    nodeCompletedCountMap.merge(record.getCourseNodeId(), 1L, Long::sum);
                }
            }
        } else {
            nodeCompletedCountMap = new HashMap<>();
        }

        List<NodeGraphDTO> nodeGraphs = nodes.stream().map(node -> {

            // 解析关联节点（逗号分隔的字符串转换为列表）
            List<Integer> relateNodeList = new ArrayList<>();
            if (node.getRelateNode() != null && !node.getRelateNode().isEmpty()) {
                relateNodeList = JSONUtil.toList(node.getRelateNode(), Integer.class);
            }

            // 提前获取节点ID，避免NPE风险
            final Integer nodeId = node.getId();

            // 计算完成率
            final BigDecimal completionRate = totalStudents > 0 && nodeId != null
                    ? BigDecimal.valueOf(nodeCompletedCountMap.getOrDefault(nodeId, 0L))
                    .multiply(BigDecimal.valueOf(PERCENTAGE_MULTIPLIER))
                    .divide(BigDecimal.valueOf(totalStudents), COMPLETION_RATE_SCALE, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            NodeGraphDTO dto = new NodeGraphDTO();
            dto.setNodeId(nodeId != null ? nodeId.longValue() : 0L);
            dto.setNodeName(node.getNodeName());
            dto.setRelateNode(relateNodeList);
            dto.setNodeColour(node.getNodeColour());
            dto.setNodeSize(node.getNodeSize());
            dto.setCompletionRate(completionRate);
            dto.setXAxis(node.getXAxis());
            dto.setYAxis(node.getYAxis());

            return dto;
        }).toList();

        return new KnowledgeGraphDTO(nodeGraphs, new ArrayList<>());
    }

    @Override
    public BlindSpotDTO getBlindSpots(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        // 获取本周的最开始时间，精确到秒
        // 1. 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 2. 计算本周周一的日期
        // TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        // 含义：如果今天是周一，则返回今天；否则返回上一个周一
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 3. 将时间设置为当天的最开始 (00:00:00.000000000)
        weekStart = weekStart.with(LocalTime.MIN);
        List<StudentQuestionAnswerRecordPO> records = studentQuestionAnswerRecordService.list(
                Wrappers.lambdaQuery(StudentQuestionAnswerRecordPO.class)
                        .eq(StudentQuestionAnswerRecordPO::getCourseId, courseId.intValue())
                        .ge(StudentQuestionAnswerRecordPO::getCreateTime, weekStart));

        if (records.isEmpty()) {
            return new BlindSpotDTO(new ArrayList<>());
        }

        // 提取所有问题文本
        String allQuestions = records.stream()
                .map(StudentQuestionAnswerRecordPO::getQuestion)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        // 提取高频关键词
        List<String> keywords = extractKeywords(allQuestions);

        return new BlindSpotDTO(keywords);
    }

    /**
     * 从文本中提取高频关键词
     *
     * @param text 文本内容
     * @return 高频关键词列表（最多返回10个）
     */
    private List<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        // 中文停用词列表
        Set<String> stopWords = Set.of(
                "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
                "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
                "自己", "这", "那", "什么", "怎么", "为什么", "哪", "哪个", "哪里", "如何",
                "吗", "呢", "吧", "啊", "呀", "嗯", "哦", "哈", "哎", "唉", "能不能", "可以",
                "能", "应该", "想", "知道", "请问", "老师", "同学", "大家", "我们",
                "你们", "他们", "它们", "这个", "那个", "这些", "那些", "怎样",
                "还是", "但是", "因为", "所以", "如果", "虽然", "不过", "而且",
                "以及", "并且", "或者", "不是", "就是", "只是", "还有", "已经", "一下",
                "一些", "一点", "一种", "一样", "一直", "一定", "一般", "一起"
        );

        // 使用正则表达式提取中文词汇（2-6个字的连续中文）
        // 简单分词：按非中文字符分割，然后提取连续的中文字符
        Map<String, Integer> wordCount = new HashMap<>();

        // 提取2-6个字的中文词汇
        String regex = "[\u4e00-\u9fa5]{2,6}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            // 过滤停用词和过短的词
            if (!stopWords.contains(word) && word.length() >= 2) {
                wordCount.merge(word, 1, Integer::sum);
            }
        }

        // 按词频排序，返回前10个高频词
        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public NodeDetailDTO getNodeDetail(Long courseId, Long nodeId, Long teacherId) {
        checkPermission(courseId, teacherId);

        CourseNodePO node = courseNodeService.getById(nodeId.intValue());
        if (node == null || !node.getCourseId().equals(courseId.intValue())) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }

        List<Long> studentIds = getCourseStudentIds(courseId);

        // 批量查询分组，避免 N+1 查询问题
        StudentGroupByCompletion group = splitStudentsByNodeCompletion(nodeId.intValue(), studentIds);

        BigDecimal completionRate = studentIds.isEmpty() ? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.valueOf(group.completedStudents().size())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(studentIds.size()), 1, RoundingMode.HALF_UP);

        return new NodeDetailDTO(node.getId().longValue(), node.getNodeName(),
                group.completedStudents().size(), group.incompleteStudents().size(), completionRate,
                group.completedStudents(), group.incompleteStudents());
    }

    @Override
    public List<ActivityRankingDTO> getActivityRanking(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<Long> studentIds = getCourseStudentIds(courseId);
        if (studentIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> activityScores = new HashMap<>();
        for (Long studentId : studentIds) {
            List<StudentCourseNodeStudyRecordPO> records = studentCourseNodeStudyRecordService
                    .findByCourseNodeId(studentId.intValue(), null);
            activityScores.put(studentId, records != null ? records.size() * 10 : 0);
        }

        return activityScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new ActivityRankingDTO(1, entry.getKey(), STUDENT_NAME_PREFIX + entry.getKey(), null, entry.getValue()))
                .toList();
    }

    @Override
    public ActivityHeatmapDTO getActivityHeatmap(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<Long> studentIds = getCourseStudentIds(courseId);
        if (studentIds.isEmpty()) {
            return new ActivityHeatmapDTO(Collections.emptyList(), Collections.emptyList());
        }

        // 1. 生成近7天日期范围
        List<LocalDate> dateRange = generateRecent7Days();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dateRangeStr = dateRange.stream()
                .map(d -> d.format(formatter))
                .toList();

        // 2. 计算每个学生每天的活力值
        List<ActivityHeatmapDTO.StudentHeatmapData> studentsData = calculateStudentsHeatmap(studentIds, dateRange);

        // 3. 按最近一天活力值降序排序，取前20名
        studentsData = studentsData.stream()
                .sorted((a, b) -> {
                    int lastScoreA = a.getDailyScores().getLast();
                    int lastScoreB = b.getDailyScores().getLast();
                    return Integer.compare(lastScoreB, lastScoreA);
                })
                .limit(10)
                .toList();

        return new ActivityHeatmapDTO(dateRangeStr, studentsData);
    }

    /**
     * 生成近7天日期列表（含今天）
     */
    private List<LocalDate> generateRecent7Days() {
        LocalDate today = LocalDate.now();
        return IntStream.rangeClosed(0, 6)
                .mapToObj(today::minusDays)
                .sorted()
                .toList();
    }

    /**
     * 计算学生热力图数据
     * 活力值 = min(100, 学习记录数×5 + 完成节点数×15)
     */
    private List<ActivityHeatmapDTO.StudentHeatmapData> calculateStudentsHeatmap(
            List<Long> studentIds, List<LocalDate> dateRange) {

        LocalDate startDate = dateRange.getFirst();
        LocalDate endDate = dateRange.getLast();

        // 批量查询所有学生信息，避免 N+1 查询问题
        Map<Integer, StudentPO> studentMap = studentService.listByIds(studentIds.stream().map(Long::intValue).toList())
                .stream()
                .collect(Collectors.toMap(StudentPO::getId, s -> s));

        // 批量查询所有学生的学习记录，避免 N+1 查询问题
        List<Integer> intStudentIds = studentIds.stream().map(Long::intValue).toList();
        List<StudentCourseNodeStudyRecordPO> allRecords = studentCourseNodeStudyRecordService
                .findByStudentIdsAndDateRange(intStudentIds, startDate, endDate);

        // 按学生ID分组学习记录
        Map<Integer, List<StudentCourseNodeStudyRecordPO>> recordsByStudent = allRecords.stream()
                .collect(Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId));

        return studentIds.stream().map(studentId -> {
            List<StudentCourseNodeStudyRecordPO> studyRecords = recordsByStudent.getOrDefault(studentId.intValue(), List.of());

            // 按日期分组学习记录
            Map<LocalDate, List<StudentCourseNodeStudyRecordPO>> recordsByDate = studyRecords.stream()
                    .filter(r -> r.getStudyStartTime() != null)
                    .collect(Collectors.groupingBy(r -> r.getStudyStartTime().toLocalDate()));

            // 按日期分组并统计完成的节点数（学习记录的completed字段为true）
            Map<LocalDate, Long> completedCountByDate = new HashMap<>();
            for (Map.Entry<LocalDate, List<StudentCourseNodeStudyRecordPO>> entry : recordsByDate.entrySet()) {
                long completedCount = entry.getValue().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getCompleted()))
                        .count();
                completedCountByDate.put(entry.getKey(), completedCount);
            }

            // 计算每天的活力值
            List<Integer> dailyScores = dateRange.stream()
                    .map(date -> {
                        int recordCount = recordsByDate.getOrDefault(date, Collections.emptyList()).size();
                        int completedCount = completedCountByDate.getOrDefault(date, 0L).intValue();
                        return Math.min(100, recordCount * 5 + completedCount * 15);
                    })
                    .toList();

            return new ActivityHeatmapDTO.StudentHeatmapData(
                    studentId,
                    Optional.ofNullable(studentMap.get(studentId.intValue())).map(StudentPO::getStudentName).orElse(""),
                    dailyScores
            );
        }).toList();
    }

    @Override
    public ChapterEfficiencyDTO getChapterEfficiency(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<Long> studentIds = getCourseStudentIds(courseId);
        if (studentIds.isEmpty()) {
            return new ChapterEfficiencyDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0, getProgressList(), new ArrayList<>());
        }

        // 计算所有学生的进度
        Map<String, BigDecimal> progressMap = calculateAllStudentProgress(courseId, studentIds);
        List<BigDecimal> progresses = new ArrayList<>(progressMap.values());

        // 课程平均完成度
        BigDecimal courseCompletion = progresses.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(studentIds.size()), 1, RoundingMode.HALF_UP);

        // 最快进度（最大值）
        BigDecimal fastestProgress = progresses.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);

        // 最快进度人数
        int fastestProgressCount = (int) progresses.stream()
                .filter(p -> p.compareTo(fastestProgress) == 0)
                .count();

        // 最慢进度（最小值）
        BigDecimal slowestProgress = progresses.stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);

        // 最慢进度人数
        int slowestProgressCount = (int) progresses.stream()
                .filter(p -> p.compareTo(slowestProgress) == 0)
                .count();

        // 统计各进度区间人数
        List<Integer> progressCountList = calculateProgressDistribution(progresses);

        return new ChapterEfficiencyDTO(courseCompletion, fastestProgress, fastestProgressCount,
                slowestProgress, slowestProgressCount, getProgressList(), progressCountList);
    }

    /**
     * 获取固定的进度区间列表
     */
    private List<String> getProgressList() {
        return List.of("0%", "1-20%", "21-40%", "41-60%", "61-80%", "81-99%", "100%");
    }

    /**
     * 统计各进度区间的人数
     */
    private List<Integer> calculateProgressDistribution(List<BigDecimal> progresses) {
        // 区间: [0%], [1-20%], [21-40%], [41-60%], [61-80%], [81-99%], [100%]
        int[] counts = new int[7];

        for (BigDecimal progress : progresses) {
            double value = progress.doubleValue();
            if (value == 0) {
                counts[0]++; // 0%
            } else if (value <= 20) {
                counts[1]++; // 1-20%
            } else if (value <= 40) {
                counts[2]++; // 21-40%
            } else if (value <= 60) {
                counts[3]++; // 41-60%
            } else if (value <= 80) {
                counts[4]++; // 61-80%
            } else if (value < 100) {
                counts[5]++; // 81-99%
            } else {
                counts[6]++; // 100%
            }
        }

        return Arrays.stream(counts).boxed().collect(Collectors.toList());
    }

    @Override
    public List<HomeworkStatisticsDTO> getCourseOverview(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        // 获取课程关联的学生ID列表
        List<Long> studentIds = getCourseStudentIds(courseId);
        int totalStudents = studentIds.size();

        // 获取课程的所有作业
        List<HomeworkPO> homeworkList = homeworkService.listByCourseId(courseId.intValue());

        if (homeworkList.isEmpty()) {
            return new ArrayList<>();
        }

        // 遍历作业，计算统计数据
        return homeworkList.stream().map(homework -> {
            // 查询该作业的提交记录
            List<HomeworkSubmitPO> submits = homeworkSubmitService.listByHomeworkId(homework.getId());

            // 统计已提交人数（去重）
            int submittedCount = (int) submits.stream()
                    .map(HomeworkSubmitPO::getStudentId)
                    .distinct()
                    .count();

            // 计算提交率
            BigDecimal submitRate = totalStudents == 0 ? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP) :
                    BigDecimal.valueOf(submittedCount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalStudents), 1, RoundingMode.HALF_UP);

            // 计算未提交人数
            int unsubmittedCount = totalStudents - submittedCount;

            return new HomeworkStatisticsDTO(
                    homework.getId().longValue(),
                    homework.getTitle(),
                    submitRate,
                    submittedCount,
                    unsubmittedCount
            );
        }).toList();
    }

    @Override
    public TaskFlowDTO getTaskFlow(Long courseId, Long studentId, Long teacherId) {
        checkPermission(courseId, teacherId);
        // 校验 studentId 属于本课程所在学校，防止跨租户读取学习数据
        assertStudentInCourseSchool(courseId, studentId);

        List<CourseNodePO> nodes = getCourseNodes(courseId);

        List<TaskNodeDTO> taskNodes = nodes.stream().map(node -> {
            Optional<StudentCourseNodeStudyRecordPO> recordOpt = studentCourseNodeStudyRecordService
                    .findLastRecord(studentId.intValue(), node.getId());
            int status = 0;
            LocalDateTime recordTime = null;
            if (recordOpt.isPresent()) {
                StudentCourseNodeStudyRecordPO record = recordOpt.get();
                if (Boolean.TRUE.equals(record.getCompleted())) {
                    status = 2; // 已完成
                }
                recordTime = record.getStudyStartTime();
            }
            return new TaskNodeDTO(node.getId().longValue(), node.getNodeName(), status,
                    recordTime, null);
        }).toList();

        return new TaskFlowDTO(studentId, STUDENT_NAME_PREFIX + studentId, taskNodes);
    }

    @Override
    public NodeStudentsDTO getNodeStudents(Long courseId, Long nodeId, Long teacherId) {
        checkPermission(courseId, teacherId);

        CourseNodePO node = courseNodeService.getById(nodeId.intValue());
        if (node == null || !node.getCourseId().equals(courseId.intValue())) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }

        List<Long> studentIds = getCourseStudentIds(courseId);

        // 批量查询分组，避免 N+1 查询问题
        StudentGroupByCompletion group = splitStudentsByNodeCompletion(nodeId.intValue(), studentIds);

        return new NodeStudentsDTO(nodeId, node.getNodeName(), group.completedStudents(), group.incompleteStudents());
    }

    @Override
    public RiskWarningListDTO getRiskWarnings(Long courseId, Long teacherId) {
        checkPermission(courseId, teacherId);

        List<StudyRiskWarningPO> warnings = studyRiskWarningService.listByCourseId(courseId);

        if (warnings.isEmpty()) {
            return new RiskWarningListDTO(new ArrayList<>(), 0, 0, 0, 0);
        }

        // 批量获取学生信息
        List<Integer> studentIds = warnings.stream()
                .map(w -> w.getStudentId().intValue())
                .distinct()
                .toList();
        Map<Integer, StudentPO> studentMap = studentService.listByIds(studentIds)
                .stream()
                .collect(Collectors.toMap(StudentPO::getId, s -> s));

        // 批量获取节点信息
        List<Integer> nodeIds = warnings.stream()
                .map(w -> w.getNodeId() != null ? w.getNodeId().intValue() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, CourseNodePO> nodeMap = nodeIds.isEmpty() ? new HashMap<>() :
                courseNodeService.listByIds(nodeIds).stream()
                        .collect(Collectors.toMap(CourseNodePO::getId, n -> n));

        List<RiskWarningItemDTO> items = warnings.stream()
                .map(w -> {
                    StudentPO student = studentMap.get(w.getStudentId().intValue());
                    String studentNo = student != null ? student.getIdNumber() : "";
                    String studentName = student != null ? student.getStudentName() : STUDENT_NAME_PREFIX + w.getStudentId();
                    String avatar = null; // StudentPO 没有 avatar 字段

                    // 获取节点名称
                    String nodeName = null;
                    if (w.getNodeId() != null) {
                        CourseNodePO node = nodeMap.get(w.getNodeId().intValue());
                        nodeName = node != null ? node.getNodeName() : null;
                    }

                    // 如果风险原因为空，根据风险类型生成默认原因
                    String riskReason = w.getRiskReason();
                    if (riskReason == null || riskReason.isBlank()) {
                        riskReason = generateDefaultRiskReason(w.getRiskType(), nodeName);
                    }

                    return new RiskWarningItemDTO(w.getId(), w.getStudentId(), studentNo, studentName, avatar,
                            w.getRiskLevel(), w.getRiskType(), riskReason,
                            w.getNodeId(), nodeName, w.getStatus(), w.getHandlerId(),
                            null, w.getHandleTime(), w.getCreateTime());
                })
                .toList();

        long highCount = warnings.stream().filter(w -> RISK_HIGH.equals(w.getRiskLevel())).count();
        long mediumCount = warnings.stream().filter(w -> RISK_MEDIUM.equals(w.getRiskLevel())).count();
        long lowCount = warnings.stream().filter(w -> RISK_LOW.equals(w.getRiskLevel())).count();

        return new RiskWarningListDTO(items, (int) highCount, (int) mediumCount, (int) lowCount, warnings.size());
    }

    /**
     * 根据风险类型生成默认风险原因
     */
    private String generateDefaultRiskReason(String riskType, String nodeName) {
        if (riskType == null) {
            return "学习异常";
        }
        return switch (riskType) {
            case "stuck" -> "在节点「" + (nodeName != null ? nodeName : "未知") + "」卡关超过阈值时间";
            case "lagging" -> "学习进度严重滞后，低于班级平均水平";
            case "inactive" -> "长时间未进行学习活动";
            default -> "学习异常";
        };
    }

    @Override
    public boolean handleRiskWarning(Long id, Long teacherId) {
        // 通过预警记录回溯到课程再做权限校验，防止跨租户处理预警
        StudyRiskWarningPO warning = studyRiskWarningService.getById(id);
        if (warning == null || warning.getCourseId() == null) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        checkPermission(warning.getCourseId(), teacherId);
        return studyRiskWarningService.handleWarning(id, teacherId);
    }

    // ========== 私有辅助方法 ==========

    private void checkPermission(Long courseId, Long teacherId) {
        CoursePO course = courseService.getById(courseId.intValue());
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        // 先加载并校验当前后台用户角色，防止 ID 碰撞绕过角色校验：
        // 仅 role=0（超管）和 role=1（学校管理员）可访问学情监控接口；role=2 教师/学生直接拒绝。
        UserPO userPO = userService.getById(teacherId.intValue());
        if (Objects.isNull(userPO)) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        Integer role = userPO.getRole();
        if (!Objects.equals(role, 0) && !Objects.equals(role, 1)) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        // 课程创建者：放行（教师创建者必须是合法后台用户）
        if (course.getCreatorId() != null && course.getCreatorId().equals(teacherId.intValue())) {
            return;
        }
        if (Objects.equals(role, 0)) {
            // 超管：放行
            return;
        }
        // 学校管理员：仅可访问本校课程。租户归属以 DB 中 userPO.getSchoolId() 为准，
        // 避免依赖可能被篡改或缺失的 session。
        Integer currentSchoolId = userPO.getSchoolId();
        Integer courseSchoolId = course.getSchoolId();
        if (currentSchoolId != null && currentSchoolId > 0
                && courseSchoolId != null && currentSchoolId.equals(courseSchoolId)) {
            return;
        }
        throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
    }

    /**
     * 校验 studentId 与 courseId 同属一个学校且学生确实选了该课程，防止跨租户或越权读取学习数据。
     * 超管（role=0）可绕过；其余角色必须满足：
     *   1. course.schoolId == student.schoolId
     *   2. course_student_relation(courseId, studentId) 存在
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     */
    private void assertStudentInCourseSchool(Long courseId, Long studentId) {
        if (courseId == null || studentId == null) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        Integer currentUserId = StpUtil.getLoginIdAsInt();
        UserPO currentUser = userService.getById(currentUserId);
        if (currentUser != null && Integer.valueOf(0).equals(currentUser.getRole())) {
            return; // 超管放行
        }
        CoursePO course = courseService.getById(courseId.intValue());
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        StudentPO student = studentService.getById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }
        // 1. 同校校验
        if (!Objects.equals(course.getSchoolId(), student.getSchoolId())) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        // 2. 选课关系校验：防止跨课程读取他人学习数据
        boolean enrolled = courseStudentRelationService.list(
                        Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                                .eq(CourseStudentRelationPO::getCourseId, courseId.intValue())
                                .eq(CourseStudentRelationPO::getStudentId, studentId.intValue()))
                .stream()
                .findFirst()
                .isPresent();
        if (!enrolled) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
    }

    /**
     * 实时计算学生在某课程的进度（已完成内容节点数 / 内容节点总数 × 100）。
     * 不依赖 study_progress_snapshot（该表无定时任务写入，始终为空）。
     */
    private BigDecimal calculateStudentProgress(Integer studentId, Integer courseId) {
        List<CourseNodePO> allNodes = courseNodeService.queryByCourseId(courseId);
        List<CourseNodePO> validNodes = allNodes.stream()
                .filter(node -> !node.startNode() && !node.endNode())
                .toList();
        if (validNodes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Set<Integer> courseNodeIds = allNodes.stream().map(CourseNodePO::getId).collect(Collectors.toSet());
        List<StudentCourseNodeStudyRecordPO> records = studentCourseNodeStudyRecordService.list(
                Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                        .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                        .eq(StudentCourseNodeStudyRecordPO::getCompleted, true)
                        .in(StudentCourseNodeStudyRecordPO::getCourseNodeId, courseNodeIds));
        long studiedCount = records.stream()
                .map(StudentCourseNodeStudyRecordPO::getCourseNodeId)
                .distinct()
                .count();
        return BigDecimal.valueOf(studiedCount * 100.0 / validNodes.size())
                .setScale(1, RoundingMode.HALF_UP);
    }

    private CoreIndicatorOverviewDTO emptyOverview() {
        return new CoreIndicatorOverviewDTO(
                new ProgressIndicatorDTO(BigDecimal.ZERO, BigDecimal.ZERO, 0, 0),
                new ActivityIndicatorDTO(0, 0, 0, 0, 0),
                new StuckIndicatorDTO(0, 0, 0, 0),
                new LaggingIndicatorDTO(0, 0, 0, 0)
        );
    }

    /**
     * 批量计算所有学生的进度
     */
    private Map<String, BigDecimal> calculateAllStudentProgress(Long courseId, List<Long> studentIds) {
        List<CourseNodePO> nodes = getCourseNodes(courseId);
        if (nodes.isEmpty()) {
            return studentIds.stream().collect(Collectors.toMap(Object::toString, sid -> BigDecimal.ZERO));
        }

        // 批量获取所有学习记录
        List<Integer> nodeIds = nodes.stream().map(CourseNodePO::getId).collect(Collectors.toList());
        List<StudentCourseNodeStudyRecordPO> allRecords = studentCourseNodeStudyRecordService.list(
                Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                        .in(StudentCourseNodeStudyRecordPO::getCourseNodeId, nodeIds)
                        .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds.stream().map(Long::intValue).collect(Collectors.toList()))
        );

        // 按学生ID和节点ID分组，取最新的记录
        Map<String, StudentCourseNodeStudyRecordPO> latestRecordMap = new HashMap<>();
        for (StudentCourseNodeStudyRecordPO record : allRecords) {
            String key = record.getStudentId() + "_" + record.getCourseNodeId();
            StudentCourseNodeStudyRecordPO existing = latestRecordMap.get(key);
            if (existing == null || Objects.equals(existing.getCompleted(), Boolean.TRUE)) {
                latestRecordMap.put(key, record);
            }
        }

        // 计算每个学生的完成节点数
        Map<String, Long> studentCompletedCount = new HashMap<>();
        for (Map.Entry<String, StudentCourseNodeStudyRecordPO> entry : latestRecordMap.entrySet()) {
            StudentCourseNodeStudyRecordPO record = entry.getValue();
            if (Boolean.TRUE.equals(record.getCompleted())) {
                String studentKey = record.getStudentId().toString();
                studentCompletedCount.merge(studentKey, 1L, Long::sum);
            }
        }

        Map<String, BigDecimal> progressMap = new HashMap<>();
        for (Long studentId : studentIds) {
            String key = studentId.toString();
            long completedCount = studentCompletedCount.getOrDefault(key, 0L);
            BigDecimal progress = BigDecimal.valueOf(completedCount * 100.0 / nodes.size())
                    .setScale(1, RoundingMode.HALF_UP);
            progressMap.put(key, progress);
        }
        return progressMap;
    }

    private ActivityIndicatorDTO calculateActivity(Long courseId, List<Long> studentIds) {
        Set<Long> activeStudentIds = new HashSet<>();
        for (Long studentId : studentIds) {
            List<StudentCourseNodeStudyRecordPO> records = studentCourseNodeStudyRecordService
                    .findByCourseNodeId(studentId.intValue(), null);
            if (records != null && !records.isEmpty()) {
                activeStudentIds.add(studentId);
            }
        }

        int activityIndex = studentIds.isEmpty() ? 0 : Math.min(100, activeStudentIds.size() * 100 / studentIds.size());
        return new ActivityIndicatorDTO(activityIndex, 0, 0, 0, activeStudentIds.size());
    }

    private StuckIndicatorDTO calculateStuckIndicators(Long courseId, List<Long> studentIds) {
        List<CourseNodePO> nodes = getCourseNodes(courseId);
        if (nodes.isEmpty()) {
            return new StuckIndicatorDTO(0, 0, 0, 0);
        }

        int stuckCount = 0, highRisk = 0, mediumRisk = 0, lowRisk = 0;

        for (Long studentId : studentIds) {
            for (CourseNodePO node : nodes) {
                Optional<StudentCourseNodeStudyRecordPO> recordOpt = studentCourseNodeStudyRecordService
                        .findLastRecord(studentId.intValue(), node.getId());
                if (recordOpt.isPresent() && !isNodeCompleted(node.getId(), studentId.intValue())) {
                    StudentCourseNodeStudyRecordPO record = recordOpt.get();
                    if (record.getStudyStartTime() != null) {
                        long hours = java.time.Duration.between(record.getStudyStartTime(), java.time.LocalDateTime.now()).toHours();
                        if (hours > STUCK_HIGH_RISK_HOURS) highRisk++;
                        else if (hours > STUCK_MEDIUM_RISK_HOURS) mediumRisk++;
                        else if (hours > STUCK_LOW_RISK_HOURS) lowRisk++;
                        if (hours > STUCK_LOW_RISK_HOURS) stuckCount++;
                    }
                }
            }
        }

        return new StuckIndicatorDTO(stuckCount, highRisk, mediumRisk, lowRisk);
    }

    private LaggingIndicatorDTO calculateLaggingIndicators(Map<String, BigDecimal> progressMap, int totalStudents) {
        if (totalStudents == 0) {
            return new LaggingIndicatorDTO(0, 0, 0, 0);
        }

        int laggingCount = 0, highRisk = 0, mediumRisk = 0, lowRisk = 0;

        for (BigDecimal progress : progressMap.values()) {
            double progressValue = progress.doubleValue();
            if (progressValue < LAGGING_LOW_THRESHOLD) {
                laggingCount++;
                if (progressValue < LAGGING_HIGH_THRESHOLD) highRisk++;
                else if (progressValue < LAGGING_MEDIUM_THRESHOLD) mediumRisk++;
                else lowRisk++;
            }
        }

        return new LaggingIndicatorDTO(laggingCount, highRisk, mediumRisk, lowRisk);
    }

    @Override
    public StudentProgressListVO getStudentProgressList(Long courseId, Long teacherId,
            String filterType, String studentName, String studentNo,
            String className, Integer pageNum, Integer pageSize) {
        // 添加权限检查
        checkPermission(courseId, teacherId);

        // 1. 获取课程关联的学生ID列表
        List<Long> courseStudentIds = getCourseStudentIds(courseId);

        if (courseStudentIds.isEmpty()) {
            StudentProgressListVO vo = new StudentProgressListVO();
            vo.setTotal(0);
            vo.setWarningCount(0);
            vo.setList(Collections.emptyList());
            return vo;
        }

        // 2. 查询所有学生（不分页）以计算预警
        LambdaQueryWrapper<StudentPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(StudentPO::getId, courseStudentIds);
        if (studentName != null && !studentName.isEmpty()) {
            queryWrapper.like(StudentPO::getStudentName, studentName);
        }
        if (studentNo != null && !studentNo.isEmpty()) {
            queryWrapper.like(StudentPO::getIdNumber, studentNo);
        }
        // 根据班级名称从 school_class 表查询
        if (className != null && !className.isEmpty()) {
            List<SchoolClassPO> schoolClasses = schoolClassService.list(
                    Wrappers.lambdaQuery(SchoolClassPO.class)
                            .like(SchoolClassPO::getClassName, className));
            if (!schoolClasses.isEmpty()) {
                List<Integer> classIds = schoolClasses.stream()
                        .map(SchoolClassPO::getId)
                        .collect(Collectors.toList());
                queryWrapper.in(StudentPO::getClassId, classIds);
            } else {
                // 没有匹配的班级，返回空结果
                StudentProgressListVO vo = new StudentProgressListVO();
                vo.setTotal(0);
                vo.setWarningCount(0);
                vo.setList(Collections.emptyList());
                return vo;
            }
        }
        List<StudentPO> allStudents = studentService.list(queryWrapper);

        if (allStudents.isEmpty()) {
            StudentProgressListVO vo = new StudentProgressListVO();
            vo.setTotal(0);
            vo.setWarningCount(0);
            vo.setList(Collections.emptyList());
            return vo;
        }

        List<Integer> studentIds = allStudents.stream().map(StudentPO::getId).toList();

        // ========== 批量查询阶段：消除N+1查询问题 ==========

        // 1. 批量查询作业列表（课程下所有作业）- 只查一次
        List<HomeworkPO> homeworks = homeworkService.list(Wrappers.lambdaQuery(HomeworkPO.class)
                .eq(HomeworkPO::getCourseId, courseId.intValue()));
        List<Integer> homeworkIds = homeworks.stream().map(HomeworkPO::getId).toList();

        // 2. 批量查询班级信息
        List<Integer> classIds = allStudents.stream()
                .map(StudentPO::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> classNameMap = classIds.isEmpty() ? Map.of() :
                schoolClassService.listByIds(classIds).stream()
                        .collect(Collectors.toMap(SchoolClassPO::getId, SchoolClassPO::getClassName, (a, b) -> a));

        // 3. 批量查询学习进度快照
        List<StudyProgressSnapshotPO> progressSnapshots = studyProgressSnapshotService.listLatestByCourseAndStudentIds(courseId, studentIds.stream().map(Long::valueOf).toList());
        Map<Integer, StudyProgressSnapshotPO> progressMap = progressSnapshots.stream()
                .collect(Collectors.toMap(p -> p.getStudentId().intValue(), p -> p, (a, b) -> a));

        // 4. 批量查询作业提交（所有学生的提交）
        List<HomeworkSubmitPO> allSubmits = homeworkIds.isEmpty() ? List.of() :
                homeworkSubmitService.list(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                        .in(HomeworkSubmitPO::getStudentId, studentIds)
                        .in(HomeworkSubmitPO::getHomeworkId, homeworkIds));
        Map<Integer, Long> submitCountMap = allSubmits.stream()
                .collect(Collectors.groupingBy(HomeworkSubmitPO::getStudentId, Collectors.counting()));

        // 5. 批量查询最新学习记录
        List<StudentCourseNodeStudyRecordPO> lastRecords = studentCourseNodeStudyRecordService.findLastRecordsByStudentIds(studentIds);
        Map<Integer, StudentCourseNodeStudyRecordPO> lastRecordMap = lastRecords.stream()
                .collect(Collectors.toMap(StudentCourseNodeStudyRecordPO::getStudentId, r -> r, (a, b) -> a));

        // 6. 批量查询课程节点（用于获取当前学习节点名称）
        List<Integer> nodeIds = lastRecords.stream()
                .map(StudentCourseNodeStudyRecordPO::getCourseNodeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> nodeNameMap = nodeIds.isEmpty() ? Map.of() :
                courseNodeService.listByIds(nodeIds).stream()
                        .collect(Collectors.toMap(CourseNodePO::getId, CourseNodePO::getNodeName, (a, b) -> a));

        // ========== 组装阶段 ==========
        // 批量构建所有学生的进度信息并统计预警
        Map<Long, StudentProgressDTO> allProgressMap = new HashMap<>();
        int warningCount = 0;

        for (StudentPO student : allStudents) {
            StudentProgressDTO dto = buildStudentProgressDTOWithPreloadedData(
                    student, courseId, classNameMap, progressMap, homeworks.size(), submitCountMap, lastRecordMap, nodeNameMap);
            allProgressMap.put(dto.getStudentId(), dto);
            if (dto.getIsWarning()) {
                warningCount++;
            }
        }

        // 根据 filterType 过滤学生
        List<Long> filteredStudentIds;
        if ("warning".equals(filterType)) {
            filteredStudentIds = allProgressMap.values().stream()
                    .filter(StudentProgressDTO::getIsWarning)
                    .map(StudentProgressDTO::getStudentId)
                    .collect(Collectors.toList());
        } else {
            filteredStudentIds = allStudents.stream()
                    .map(StudentPO::getId)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        // 分页处理
        int total = filteredStudentIds.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<StudentProgressDTO> resultList = new ArrayList<>();
        if (fromIndex < toIndex) {
            List<Long> pageStudentIds = filteredStudentIds.subList(fromIndex, toIndex);
            for (Long sid : pageStudentIds) {
                resultList.add(allProgressMap.get(sid));
            }
        }

        // 构建返回结果
        StudentProgressListVO vo = new StudentProgressListVO();
        vo.setTotal(total);
        vo.setWarningCount(warningCount);
        vo.setList(resultList);
        return vo;
    }

    @Override
    public StudentLearningDetailVO getStudentLearningDetail(Long studentId, Long courseId, Long teacherId) {
        // 添加权限检查
        checkPermission(courseId, teacherId);
        // 校验 studentId 属于本课程所在学校，防止跨租户读取学习详情
        assertStudentInCourseSchool(courseId, studentId);

        StudentPO student = studentService.getById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }

        StudentLearningDetailVO vo = new StudentLearningDetailVO();
        vo.setStudentName(student.getStudentName());
        vo.setLastActiveTime(student.getLastLoginTime());

        // 实时计算课程进度（不依赖快照表）
        BigDecimal courseProgress = calculateStudentProgress(studentId.intValue(), courseId.intValue());
        vo.setCourseProgress(courseProgress);

        // 获取作业统计
        List<HomeworkPO> homeworks = homeworkService.list(Wrappers.lambdaQuery(HomeworkPO.class)
                .eq(HomeworkPO::getCourseId, courseId.intValue()));

        // homeworks 为空时不能 .in(空集合)，否则生成 IN () → SQL 语法错
        List<HomeworkSubmitPO> submits = homeworks.isEmpty() ? Collections.emptyList() :
                homeworkSubmitService.list(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                        .eq(HomeworkSubmitPO::getStudentId, studentId.intValue())
                        .in(HomeworkSubmitPO::getHomeworkId, homeworks.stream().map(HomeworkPO::getId).collect(Collectors.toList())));

        // 计算提交率
        if (homeworks.isEmpty()) {
            vo.setAssignmentSubmitRate(BigDecimal.ZERO);
        } else {
            BigDecimal submitRate = BigDecimal.valueOf(submits.size())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(homeworks.size()), 1, RoundingMode.HALF_UP);
            vo.setAssignmentSubmitRate(submitRate);
        }

        // 获取节点进度列表
        List<CourseNodePO> courseNodes = getCourseNodes(courseId);
        List<CourseNodeProgressDTO> nodeProgressList = new ArrayList<>();

        for (CourseNodePO node : courseNodes) {
            if (Objects.equals("开始", node.getNodeName()) || Objects.equals("结束", node.getNodeName())) {
                continue;
            }
            CourseNodeProgressDTO nodeDto = new CourseNodeProgressDTO();
            nodeDto.setNodeId(node.getId().longValue());
            nodeDto.setNodeName(node.getNodeName());
            nodeDto.setNodeColour(node.getNodeColour());

            // 查询学习记录
            List<StudentCourseNodeStudyRecordPO> records = studentCourseNodeStudyRecordService.list(
                    Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                            .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId.intValue())
                            .eq(StudentCourseNodeStudyRecordPO::getCourseNodeId, node.getId()));

            if (records.isEmpty()) {
                nodeDto.setStatus(0); // 未开始
                nodeDto.setDuration(0);
                nodeDto.setProgress(BigDecimal.ZERO);
            } else {
                // 取最新的学习记录
                StudentCourseNodeStudyRecordPO latestRecord = records.getFirst();
                nodeDto.setDuration(latestRecord.getStudyTime());

                if (latestRecord.getStudyEndTime() != null) {
                    nodeDto.setStatus(2); // 已完成
                    nodeDto.setProgress(BigDecimal.valueOf(100));
                } else {
                    nodeDto.setStatus(1); // 进行中
                    nodeDto.setProgress(BigDecimal.valueOf(50));
                }
            }
            nodeProgressList.add(nodeDto);
        }
        vo.setNodeProgressList(nodeProgressList);

        // 获取作业记录列表
        List<AssignmentRecordDTO> assignmentRecords = new ArrayList<>();
        for (HomeworkPO homework : homeworks) {
            AssignmentRecordDTO recordDto = new AssignmentRecordDTO();
            recordDto.setAssignmentId(homework.getId().longValue());
            recordDto.setTitle(homework.getTitle());
            recordDto.setDeadline(homework.getDeadline());

            // 查找提交记录
            Optional<HomeworkSubmitPO> submitOpt = submits.stream()
                    .filter(s -> s.getHomeworkId().equals(homework.getId()))
                    .findFirst();

            if (submitOpt.isPresent()) {
                HomeworkSubmitPO submit = submitOpt.get();
                recordDto.setSubmitTime(submit.getSubmitTime());
                if (submit.getScore() != null) {
                    recordDto.setStatus(2); // 已批改
                    recordDto.setScore(submit.getScore());
                } else {
                    recordDto.setStatus(1); // 已提交
                }
            } else {
                recordDto.setStatus(0); // 未提交
            }
            assignmentRecords.add(recordDto);
        }
        // 按截止时间倒序
        assignmentRecords.sort((a, b) -> b.getDeadline().compareTo(a.getDeadline()));
        vo.setAssignmentRecords(assignmentRecords);

        return vo;
    }

    /**
     * 构建学生进度DTO
     */
    private StudentProgressDTO buildStudentProgressDTO(StudentPO student, Long courseId) {
        StudentProgressDTO dto = new StudentProgressDTO();
        dto.setStudentId(student.getId().longValue());
        dto.setStudentName(student.getStudentName());
        dto.setStudentNo(student.getIdNumber());
        // 从 school_class 表获取班级名称
        if (student.getClassId() != null) {
            SchoolClassPO schoolClass = schoolClassService.getById(student.getClassId());
            if (schoolClass != null) {
                dto.setClassName(schoolClass.getClassName());
            } else {
                dto.setClassName(null);
            }
        } else {
            dto.setClassName(null);
        }
        dto.setLastLoginTime(student.getLastLoginTime());

        // 获取课程进度
        // 实时计算课程进度
        BigDecimal courseProgress = calculateStudentProgress(student.getId(), courseId.intValue());
        dto.setCourseProgress(courseProgress);

        // 获取作业统计
        List<HomeworkPO> homeworks = homeworkService.list(Wrappers.lambdaQuery(HomeworkPO.class)
                .eq(HomeworkPO::getCourseId, courseId.intValue()));
        dto.setTotalAssignmentCount(homeworks.size());

        if (CollectionUtils.isEmpty(homeworks)) {
            dto.setSubmittedCount(0);
        }else {
            List<HomeworkSubmitPO> submits = homeworkSubmitService.list(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                    .eq(HomeworkSubmitPO::getStudentId, student.getId())
                    .in(HomeworkSubmitPO::getHomeworkId, homeworks.stream().map(HomeworkPO::getId).collect(Collectors.toList())));
            dto.setSubmittedCount(submits.size());
        }

        // 获取当前学习节点
        List<StudentCourseNodeStudyRecordPO> records = studentCourseNodeStudyRecordService.list(
                Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                        .eq(StudentCourseNodeStudyRecordPO::getStudentId, student.getId())
                        .orderByDesc(StudentCourseNodeStudyRecordPO::getStudyStartTime)
                        .last("LIMIT 1"));
        if (!records.isEmpty()) {
            StudentCourseNodeStudyRecordPO record = records.get(0);
            CourseNodePO node = courseNodeService.getById(record.getCourseNodeId());
            if (node != null) {
                dto.setCurrentNodeName(node.getNodeName());
            }
        }

        // 判断预警状态
        boolean isWarning = false;
        // 进度 < 50%
        if (courseProgress.compareTo(BigDecimal.valueOf(50)) < 0) {
            isWarning = true;
        }
        // 缺交数 >= 3
        int missingCount = dto.getTotalAssignmentCount() - dto.getSubmittedCount();
        if (missingCount >= 3) {
            isWarning = true;
        }
        // 连续7天未登录
        if (student.getLastLoginTime() != null) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            if (student.getLastLoginTime().isBefore(sevenDaysAgo)) {
                isWarning = true;
            }
        }
        dto.setIsWarning(isWarning);

        return dto;
    }

    /**
     * 使用预加载数据构建学生进度DTO（优化版，消除N+1查询）
     *
     * @param student         学生实体
     * @param courseId        课程ID
     * @param classNameMap    班级ID到名称的映射
     * @param progressMap     学生ID到学习进度快照的映射
     * @param homeworkCount   作业总数
     * @param submitCountMap  学生ID到提交数量的映射
     * @param lastRecordMap   学生ID到最新学习记录的映射
     * @param nodeNameMap     节点ID到节点名称的映射
     * @return 学生进度DTO
     */
    private StudentProgressDTO buildStudentProgressDTOWithPreloadedData(
            StudentPO student, Long courseId,
            Map<Integer, String> classNameMap,
            Map<Integer, StudyProgressSnapshotPO> progressMap,
            int homeworkCount,
            Map<Integer, Long> submitCountMap,
            Map<Integer, StudentCourseNodeStudyRecordPO> lastRecordMap,
            Map<Integer, String> nodeNameMap) {
        StudentProgressDTO dto = new StudentProgressDTO();
        dto.setStudentId(student.getId().longValue());
        dto.setStudentName(student.getStudentName());
        dto.setStudentNo(student.getIdNumber());

        // 从预加载的Map获取班级名称
        dto.setClassName(classNameMap.get(student.getClassId()));
        dto.setLastLoginTime(student.getLastLoginTime());

        // 实时计算课程进度（不依赖快照表，快照表为空）
        BigDecimal courseProgress = calculateStudentProgress(student.getId(), courseId.intValue());
        dto.setCourseProgress(courseProgress);

        // 使用预加载的作业总数
        dto.setTotalAssignmentCount(homeworkCount);

        // 从预加载的Map获取提交数量
        long submitCount = submitCountMap.getOrDefault(student.getId(), 0L);
        dto.setSubmittedCount((int) submitCount);

        // 从预加载的Map获取当前学习节点
        StudentCourseNodeStudyRecordPO lastRecord = lastRecordMap.get(student.getId());
        if (lastRecord != null && lastRecord.getCourseNodeId() != null) {
            dto.setCurrentNodeName(nodeNameMap.get(lastRecord.getCourseNodeId()));
        }

        // 判断预警状态
        boolean isWarning = false;
        // 进度 < 50%（仅对已开始学习的学生预警，未开始的进度为 0 不算预警）
        boolean hasStarted = lastRecordMap.get(student.getId()) != null;
        if (hasStarted && courseProgress.compareTo(BigDecimal.valueOf(50)) < 0) {
            isWarning = true;
        }
        // 缺交数 >= 3
        int missingCount = homeworkCount - (int) submitCount;
        if (missingCount >= 3) {
            isWarning = true;
        }
        // 连续7天未登录
        if (student.getLastLoginTime() != null) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            if (student.getLastLoginTime().isBefore(sevenDaysAgo)) {
                isWarning = true;
            }
        }
        dto.setIsWarning(isWarning);

        return dto;
    }

    @Override
    public void calculateKnowledgeBlindSpot() {
        LocalDate today = LocalDate.now();

        // 1. 获取所有启用的课程
        List<CoursePO> courses = courseService.enableList();
        if (courses.isEmpty()) {
            log.info("没有启用的课程，跳过知识盲区统计");
            return;
        }

        for (CoursePO course : courses) {
            Integer courseId = course.getId();

            // 2. 获取课程下的所有节点
            List<CourseNodePO> nodes = courseNodeService.queryByCourseId(courseId);
            if (nodes.isEmpty()) {
                continue;
            }

            // 3. 获取课程下的所有学生
            List<CourseStudentRelationPO> relations = courseStudentRelationService.queryByCourseIds(List.of(courseId));
            if (relations.isEmpty()) {
                continue;
            }
            List<Integer> studentIds = relations.stream()
                    .map(CourseStudentRelationPO::getStudentId)
                    .collect(Collectors.toList());
            int totalStudents = studentIds.size();

            // 4. 对每个节点统计盲区
            List<KnowledgeBlindSpotPO> blindSpotList = new ArrayList<>();
            for (CourseNodePO node : nodes) {
                // 跳过开始和结束节点
                if ("开始".equals(node.getNodeName()) || "结束".equals(node.getNodeName())) {
                    continue;
                }

                // 获取已完成的学生ID列表
                List<Integer> completedStudentIds = studentCourseNodeStudyRecordService
                        .listCompletedStudentIds(node.getId(), studentIds);

                // 计算未完成人数（盲区人数）
                int blindCount = totalStudents - completedStudentIds.size();

                // 计算盲区比例
                BigDecimal blindRate = BigDecimal.ZERO;
                if (totalStudents > 0) {
                    blindRate = BigDecimal.valueOf(blindCount)
                            .multiply(BigDecimal.valueOf(PERCENTAGE_MULTIPLIER))
                            .divide(BigDecimal.valueOf(totalStudents), 1, RoundingMode.HALF_UP);
                }

                KnowledgeBlindSpotPO blindSpot = new KnowledgeBlindSpotPO();
                blindSpot.setCourseId(courseId.longValue());
                blindSpot.setNodeId(node.getId().longValue());
                blindSpot.setNodeName(node.getNodeName());
                blindSpot.setBlindCount(blindCount);
                blindSpot.setBlindRate(blindRate);
                blindSpot.setStatisticsDate(today);
                blindSpot.setCreateTime(LocalDateTime.now());
                blindSpot.setUpdateTime(LocalDateTime.now());
                blindSpotList.add(blindSpot);
            }

            // 5. 先删除当天的旧数据
            LambdaQueryWrapper<KnowledgeBlindSpotPO> deleteWrapper = Wrappers.lambdaQuery();
            deleteWrapper.eq(KnowledgeBlindSpotPO::getCourseId, courseId.longValue())
                    .eq(KnowledgeBlindSpotPO::getStatisticsDate, today);
            knowledgeBlindSpotService.remove(deleteWrapper);

            // 6. 保存新数据
            if (!blindSpotList.isEmpty()) {
                knowledgeBlindSpotService.saveBatch(blindSpotList);
            }
        }

        log.info("知识盲区统计任务完成，共处理 {} 个课程", courses.size());
    }

    @Override
    public void calculateRiskWarning() {
        // 1. 获取所有启用的课程
        List<CoursePO> courses = courseService.enableList();
        if (courses.isEmpty()) {
            log.info("没有启用的课程，跳过风险预警统计");
            return;
        }

        int totalWarnings = 0;

        for (CoursePO course : courses) {
            Integer courseId = course.getId();

            // 2. 获取课程下的所有学生及其学习进度
            List<CourseStudentRelationPO> relations = courseStudentRelationService.queryByCourseIds(List.of(courseId));
            if (relations.isEmpty()) {
                continue;
            }

            List<Integer> studentIds = relations.stream()
                    .map(CourseStudentRelationPO::getStudentId)
                    .toList();

            // 转换为 Long 类型
            List<Long> studentIdLongs = studentIds.stream()
                    .map(Integer::longValue)
                    .toList();

            // 3. 获取课程进度快照（用于判断进度滞后）
            Map<Long, StudyProgressSnapshotPO> progressMap = studyProgressSnapshotService
                    .listLatestByCourseAndStudentIds(courseId.longValue(), studentIdLongs)
                    .stream()
                    .collect(Collectors.toMap(StudyProgressSnapshotPO::getStudentId, Function.identity(), (a, b) -> a));

            // 4. 获取学生最后学习记录（用于判断卡关和不活跃）
            Map<Integer, StudentCourseNodeStudyRecordPO> lastRecordMap = studentCourseNodeStudyRecordService
                    .findLastRecordsByStudentIds(studentIds)
                    .stream()
                    .collect(Collectors.toMap(StudentCourseNodeStudyRecordPO::getStudentId, Function.identity(), (a, b) -> a));

            // 5. 获取学生信息（用于判断不活跃）
            Map<Integer, StudentPO> studentMap = studentService.listByIds(studentIds)
                    .stream()
                    .collect(Collectors.toMap(StudentPO::getId, Function.identity(), (a, b) -> a));

            // 6. 收集风险预警
            List<StudyRiskWarningPO> warningList = new ArrayList<>();

            for (Integer studentId : studentIds) {
                StudentPO student = studentMap.get(studentId);
                if (student == null) {
                    continue;
                }

                // 6.1 检查卡关风险
                StudentCourseNodeStudyRecordPO lastRecord = lastRecordMap.get(studentId);
                if (lastRecord != null && lastRecord.getCourseNodeId() != null) {
                    StudyRiskWarningPO stuckWarning = checkStuckRisk(studentId.longValue(), courseId.longValue(), lastRecord);
                    if (stuckWarning != null) {
                        warningList.add(stuckWarning);
                    }
                }

                // 6.2 检查进度滞后风险
                StudyRiskWarningPO laggingWarning = checkLaggingRisk(studentId.longValue(), courseId.longValue(), progressMap.get(studentId.longValue()));
                if (laggingWarning != null) {
                    warningList.add(laggingWarning);
                }

                // 6.3 检查不活跃风险
                StudyRiskWarningPO inactiveWarning = checkInactiveRisk(studentId.longValue(), courseId.longValue(), student);
                if (inactiveWarning != null) {
                    warningList.add(inactiveWarning);
                }
            }

            // 7. 先删除该课程当天的旧预警数据（按学生ID和类型）
            if (!warningList.isEmpty()) {
                List<Long> warningStudentIds = warningList.stream()
                        .map(StudyRiskWarningPO::getStudentId)
                        .distinct()
                        .toList();

                LambdaQueryWrapper<StudyRiskWarningPO> deleteWrapper = Wrappers.lambdaQuery();
                deleteWrapper.eq(StudyRiskWarningPO::getCourseId, courseId.longValue())
                        .eq(StudyRiskWarningPO::getStatus, 0) // 只删除未处理的
                        .in(StudyRiskWarningPO::getStudentId, warningStudentIds);
                studyRiskWarningService.remove(deleteWrapper);

                // 8. 保存新预警数据
                studyRiskWarningService.saveBatch(warningList);
                totalWarnings += warningList.size();
            }
        }

        log.info("风险预警统计任务完成，共生成 {} 条预警记录", totalWarnings);
    }

    /**
     * 检查卡关风险
     * 判断学生在当前节点停留时间是否过长
     */
    private StudyRiskWarningPO checkStuckRisk(Long studentId, Long courseId, StudentCourseNodeStudyRecordPO lastRecord) {
        if (lastRecord.getStudyStartTime() == null) {
            return null;
        }

        long hoursSinceLastStudy = java.time.Duration.between(lastRecord.getStudyStartTime(), LocalDateTime.now()).toHours();

        String riskLevel;
        if (hoursSinceLastStudy > STUCK_HIGH_RISK_HOURS) {
            riskLevel = RISK_HIGH;
        } else if (hoursSinceLastStudy > STUCK_MEDIUM_RISK_HOURS) {
            riskLevel = RISK_MEDIUM;
        } else if (hoursSinceLastStudy > STUCK_LOW_RISK_HOURS) {
            riskLevel = RISK_LOW;
        } else {
            return null; // 未达到预警阈值
        }

        // 获取节点名称
        CourseNodePO node = courseNodeService.getById(lastRecord.getCourseNodeId());
        String nodeName = node != null ? node.getNodeName() : "未知节点";

        StudyRiskWarningPO warning = new StudyRiskWarningPO();
        warning.setStudentId(studentId);
        warning.setCourseId(courseId);
        warning.setRiskType("stuck");
        warning.setRiskLevel(riskLevel);
        warning.setRiskReason(String.format("学生在节点「%s」停留超过%d小时", nodeName, hoursSinceLastStudy));
        warning.setNodeId(lastRecord.getCourseNodeId().longValue());
        warning.setStatus(0);
        warning.setCreateTime(LocalDateTime.now());
        warning.setUpdateTime(LocalDateTime.now());
        return warning;
    }

    /**
     * 检查进度滞后风险
     * 判断学生当前进度是否落后于预期
     */
    private StudyRiskWarningPO checkLaggingRisk(Long studentId, Long courseId, StudyProgressSnapshotPO progress) {
        if (progress == null || progress.getProgress() == null) {
            return null;
        }

        BigDecimal currentProgress = progress.getProgress();
        String riskLevel;
        if (currentProgress.compareTo(BigDecimal.valueOf(LAGGING_HIGH_THRESHOLD)) < 0) {
            riskLevel = RISK_HIGH;
        } else if (currentProgress.compareTo(BigDecimal.valueOf(LAGGING_MEDIUM_THRESHOLD)) < 0) {
            riskLevel = RISK_MEDIUM;
        } else if (currentProgress.compareTo(BigDecimal.valueOf(LAGGING_LOW_THRESHOLD)) < 0) {
            riskLevel = RISK_LOW;
        } else {
            return null; // 未达到预警阈值
        }

        StudyRiskWarningPO warning = new StudyRiskWarningPO();
        warning.setStudentId(studentId);
        warning.setCourseId(courseId);
        warning.setRiskType("lagging");
        warning.setRiskLevel(riskLevel);
        warning.setRiskReason(String.format("学生当前进度为%.1f%%，低于预期", currentProgress));
        warning.setStatus(0);
        warning.setCreateTime(LocalDateTime.now());
        warning.setUpdateTime(LocalDateTime.now());
        return warning;
    }

    /**
     * 检查不活跃风险
     * 判断学生是否长时间未登录
     */
    private StudyRiskWarningPO checkInactiveRisk(Long studentId, Long courseId, StudentPO student) {
        if (student.getLastLoginTime() == null) {
            return null;
        }

        long daysSinceLastLogin = java.time.Duration.between(student.getLastLoginTime(), LocalDateTime.now()).toDays();

        String riskLevel;
        if (daysSinceLastLogin > 7) {
            riskLevel = RISK_HIGH;
        } else if (daysSinceLastLogin > 3) {
            riskLevel = RISK_MEDIUM;
        } else if (daysSinceLastLogin > 1) {
            riskLevel = RISK_LOW;
        } else {
            return null; // 未达到预警阈值
        }

        StudyRiskWarningPO warning = new StudyRiskWarningPO();
        warning.setStudentId(studentId);
        warning.setCourseId(courseId);
        warning.setRiskType("inactive");
        warning.setRiskLevel(riskLevel);
        warning.setRiskReason(String.format("学生已%d天未登录学习", daysSinceLastLogin));
        warning.setStatus(0);
        warning.setCreateTime(LocalDateTime.now());
        warning.setUpdateTime(LocalDateTime.now());
        return warning;
    }
}
