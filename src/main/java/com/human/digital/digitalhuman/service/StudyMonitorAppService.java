package com.human.digital.digitalhuman.service;

import com.human.digital.digitalhuman.service.model.response.*;

import java.util.List;
import java.util.Map;

/**
 * 学情监控应用服务接口
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
public interface StudyMonitorAppService {

    /**
     * 获取核心指标概览
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 核心指标概览
     */
    CoreIndicatorOverviewDTO getCoreIndicatorOverview(Long courseId, Long teacherId);

    /**
     * 获取进度趋势
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 进度趋势
     */
    ProgressTrendDTO getProgressTrend(Long courseId, Long teacherId);

    /**
     * 获取知识图谱热力图
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 知识图谱
     */
    KnowledgeGraphDTO getKnowledgeGraph(Long courseId, Long teacherId);

    /**
     * 获取高频知识盲区
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 知识盲区列表
     */
    BlindSpotDTO getBlindSpots(Long courseId, Long teacherId);

    /**
     * 获取节点详情
     *
     * @param courseId 课程ID
     * @param nodeId 节点ID
     * @param teacherId 教师ID
     * @return 节点详情
     */
    NodeDetailDTO getNodeDetail(Long courseId, Long nodeId, Long teacherId);

    /**
     * 获取问题活跃度排行
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 活跃度排行列表
     */
    List<ActivityRankingDTO> getActivityRanking(Long courseId, Long teacherId);

    /**
     * 获取学习活力热力图
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 活力热力图列表
     */
    ActivityHeatmapDTO getActivityHeatmap(Long courseId, Long teacherId);

    /**
     * 获取章节效率分布
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 章节效率列表
     */
    ChapterEfficiencyDTO getChapterEfficiency(Long courseId, Long teacherId);

    /**
     * 获取课程作业统计列表
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 作业统计列表
     */
    List<HomeworkStatisticsDTO> getCourseOverview(Long courseId, Long teacherId);

    /**
     * 获取任务完成流程图
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @param teacherId 教师ID
     * @return 任务流程
     */
    TaskFlowDTO getTaskFlow(Long courseId, Long studentId, Long teacherId);

    /**
     * 获取节点学生列表
     *
     * @param courseId 课程ID
     * @param nodeId 节点ID
     * @param teacherId 教师ID
     * @return 节点学生列表
     */
    NodeStudentsDTO getNodeStudents(Long courseId, Long nodeId, Long teacherId);

    /**
     * 获取风险预警列表
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 风险预警列表
     */
    RiskWarningListDTO getRiskWarnings(Long courseId, Long teacherId);

    /**
     * 处理风险预警
     *
     * @param id 预警ID
     * @param teacherId 处理人ID
     * @return 是否成功
     */
    boolean handleRiskWarning(Long id, Long teacherId);

    /**
     * 获取学生进度列表
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param filterType 筛选类型：all/warning
     * @param studentName 学生姓名
     * @param studentNo 学号
     * @param className 班级名称
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 学生进度列表
     */
    StudentProgressListVO getStudentProgressList(Long courseId, Long teacherId,
                                                String filterType, String studentName, String studentNo,
                                                String className, Integer pageNum, Integer pageSize);

    /**
     * 获取学生学习详情
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 学习详情
     */
    StudentLearningDetailVO getStudentLearningDetail(Long studentId, Long courseId, Long teacherId);

    /**
     * 计算知识盲区统计数据
     * 统计每个课程下各知识节点的未完成学生数和盲区比例
     */
    void calculateKnowledgeBlindSpot();

    /**
     * 计算学业风险预警统计数据
     * 统计每个学生的卡关、进度滞后、不活跃等风险并写入预警表
     */
    void calculateRiskWarning();

    /**
     * 学习时段分布（24小时按小时分桶）
     */
    Map<Integer, Integer> getLearningTimeDistribution(Long courseId, Long teacherId);

    /**
     * 学生进度排行榜（按进度降序 Top 10）
     */
    List<StudentProgressDTO> getProgressRanking(Long courseId, Long teacherId);
}
