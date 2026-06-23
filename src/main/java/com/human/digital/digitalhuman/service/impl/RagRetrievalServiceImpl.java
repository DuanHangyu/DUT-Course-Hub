package com.human.digital.digitalhuman.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.human.digital.digitalhuman.repository.mapper.*;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.service.RagRetrievalService;
import com.human.digital.digitalhuman.service.model.enums.IntentTypeEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 检索服务实现
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalServiceImpl implements RagRetrievalService {

    private final CourseMapper courseMapper;
    private final CourseNodeMapper courseNodeMapper;
    private final StudentCourseNodeStudyRecordMapper studyRecordMapper;
    private final HomeworkSubmitMapper homeworkSubmitMapper;
    private final HomeworkMapper homeworkMapper;
    private final StudentMapper studentMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final StudyProgressSnapshotMapper progressSnapshotMapper;
    private final StudyRiskWarningMapper riskWarningMapper;
    private final StudentCourseAssessMapper assessMapper;

    @Override
    public List<String> retrieve(String message, IntentTypeEnums intentType) {
        // 根据意图类型从不同数据源检索
        return switch (intentType) {
            case COURSE_QUERY -> retrieveCourseData(message);
            case STUDENT_QUERY -> retrieveStudentData(message);
            case DATA_ANALYSIS -> retrieveAnalysisData(message);
            case GENERAL -> new ArrayList<>();
        };
    }

    @Override
    public String getContext(IntentTypeEnums intentType) {
        // 预置的上下文信息
        return switch (intentType) {
            case COURSE_QUERY -> getCourseContext();
            case STUDENT_QUERY -> getStudentContext();
            case DATA_ANALYSIS -> getAnalysisContext();
            case GENERAL -> "";
        };
    }

    /**
     * 检索课程数据
     * 根据用户问题中的关键词搜索课程信息
     */
    private List<String> retrieveCourseData(String message) {
        List<String> results = new ArrayList<>();

        try {
            // 提取课程名称关键词（简化处理：直接搜索）
            String keyword = extractKeyword(message);

            // 查询课程
            LambdaQueryWrapper<CoursePO> courseWrapper = new LambdaQueryWrapper<>();
            courseWrapper.like(CoursePO::getCourseName, keyword)
                    .or()
                    .like(CoursePO::getCourseIntroduce, keyword)
                    .or()
                    .like(CoursePO::getSubject, keyword)
                    .last("LIMIT 5");

            List<CoursePO> courses = courseMapper.selectList(courseWrapper);
            for (CoursePO course : courses) {
                StringBuilder sb = new StringBuilder();
                sb.append("课程：").append(course.getCourseName());
                sb.append("，老师：").append(course.getTeacherName());
                sb.append("，介绍：").append(course.getCourseIntroduce());
                sb.append("，学科：").append(course.getSubject());
                sb.append("，学生数：").append(course.getStudentCount());
                results.add(sb.toString());
            }

            // 查询课程章节
            if (!courses.isEmpty()) {
                List<Integer> courseIds = courses.stream()
                        .map(CoursePO::getId)
                        .collect(Collectors.toList());

                LambdaQueryWrapper<CourseNodePO> nodeWrapper = new LambdaQueryWrapper<>();
                nodeWrapper.in(CourseNodePO::getCourseId, courseIds)
                        .like(CourseNodePO::getNodeName, keyword)
                        .last("LIMIT 10");

                List<CourseNodePO> nodes = courseNodeMapper.selectList(nodeWrapper);
                for (CourseNodePO node : nodes) {
                    results.add("章节：" + node.getNodeName() + "，课程ID：" + node.getCourseId());
                }
            }
        } catch (Exception e) {
            log.error("检索课程数据失败, message:{}", message, e);
        }

        return results;
    }

    /**
     * 检索学生数据
     * 根据用户问题搜索学生相关信息
     */
    private List<String> retrieveStudentData(String message) {
        List<String> results = new ArrayList<>();

        try {
            String keyword = extractKeyword(message);

            // 查询学生
            LambdaQueryWrapper<StudentPO> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.like(StudentPO::getStudentName, keyword)
                    .or()
                    .like(StudentPO::getIdNumber, keyword)
                    .last("LIMIT 10");

            List<StudentPO> students = studentMapper.selectList(studentWrapper);
            for (StudentPO student : students) {
                StringBuilder sb = new StringBuilder();
                sb.append("学生：").append(student.getStudentName());
                sb.append("，学号：").append(student.getIdNumber());
                if (student.getClassId() != null) {
                    SchoolClassPO schoolClass = schoolClassMapper.selectById(student.getClassId());
                    if (schoolClass != null) {
                        sb.append("，班级：").append(schoolClass.getClassName());
                    }
                }
                results.add(sb.toString());
            }

            // 查询作业提交情况
            if (!students.isEmpty()) {
                List<Integer> studentIds = students.stream()
                        .map(StudentPO::getId)
                        .collect(Collectors.toList());

                // 查询最近的作业
                LambdaQueryWrapper<HomeworkPO> homeworkWrapper = new LambdaQueryWrapper<>();
                homeworkWrapper.orderByDesc(HomeworkPO::getCreateTime).last("LIMIT 5");
                List<HomeworkPO> homeworks = homeworkMapper.selectList(homeworkWrapper);

                for (HomeworkPO homework : homeworks) {
                    LambdaQueryWrapper<HomeworkSubmitPO> submitWrapper = new LambdaQueryWrapper<>();
                    submitWrapper.eq(HomeworkSubmitPO::getHomeworkId, homework.getId())
                            .in(HomeworkSubmitPO::getStudentId, studentIds);
                    long submittedCount = homeworkSubmitMapper.selectCount(submitWrapper);

                    results.add("作业《" + homework.getTitle() + "》，提交人数：" + submittedCount + "/" + studentIds.size());
                }
            }
        } catch (Exception e) {
            log.error("检索学生数据失败, message:{}", message, e);
        }

        return results;
    }

    /**
     * 检索分析数据
     * 根据用户问题搜索学情分析、统计数据
     */
    private List<String> retrieveAnalysisData(String message) {
        List<String> results = new ArrayList<>();

        try {
            // 查询学习进度快照
            LambdaQueryWrapper<StudyProgressSnapshotPO> progressWrapper = new LambdaQueryWrapper<>();
            progressWrapper.orderByDesc(StudyProgressSnapshotPO::getCreateTime).last("LIMIT 10");
            List<StudyProgressSnapshotPO> snapshots = progressSnapshotMapper.selectList(progressWrapper);

            for (StudyProgressSnapshotPO snapshot : snapshots) {
                StringBuilder sb = new StringBuilder();
                sb.append("学习进度：学生ID ").append(snapshot.getStudentId());
                sb.append("，课程ID ").append(snapshot.getCourseId());
                sb.append("，进度 ").append(snapshot.getProgress()).append("%");
                results.add(sb.toString());
            }

            // 查询风险预警
            LambdaQueryWrapper<StudyRiskWarningPO> warningWrapper = new LambdaQueryWrapper<>();
            warningWrapper.orderByDesc(StudyRiskWarningPO::getCreateTime).last("LIMIT 10");

            List<StudyRiskWarningPO> warnings = riskWarningMapper.selectList(warningWrapper);
            for (StudyRiskWarningPO warning : warnings) {
                StringBuilder sb = new StringBuilder();
                sb.append("风险预警：学生ID ").append(warning.getStudentId());
                sb.append("，风险等级 ").append(warning.getRiskLevel());
                sb.append("，风险类型 ").append(warning.getRiskType());
                sb.append("，原因 ").append(warning.getRiskReason());
                results.add(sb.toString());
            }

            // 查询成绩统计
            LambdaQueryWrapper<StudentCourseAssessPO> assessWrapper = new LambdaQueryWrapper<>();
            assessWrapper.orderByDesc(StudentCourseAssessPO::getCreateTime).last("LIMIT 10");
            List<StudentCourseAssessPO> assesses = assessMapper.selectList(assessWrapper);

            for (StudentCourseAssessPO assess : assesses) {
                StringBuilder sb = new StringBuilder();
                sb.append("考核成绩：学生ID ").append(assess.getStudentId());
                sb.append("，课程ID ").append(assess.getCourseId());
                sb.append("，得分 ").append(assess.getAssessScore());
                results.add(sb.toString());
            }
        } catch (Exception e) {
            log.error("检索分析数据失败, message:{}", message, e);
        }

        return results;
    }

    /**
     * 从用户问题中提取关键词
     * 简化处理：取问题中包含的课程名、学生名等关键词
     */
    private String extractKeyword(String message) {
        // 简化处理：取问题前10个字符作为关键词
        // 实际生产环境可以使用 NLP 进行实体识别
        if (message.length() > 10) {
            return message.substring(0, 10);
        }
        return message;
    }

    private String getCourseContext() {
        return """
                课程管理包括以下信息：
                - 课程名称、课程介绍、授课老师
                - 课程章节（课程节点）、章节名称、章节内容
                - 课程类型（模板课程/学习课程）
                - 学科分类、学生人数
                - 课程状态（上架/下架）
                """;
    }

    private String getStudentContext() {
        return """
                学生管理包括以下信息：
                - 学生姓名、学号
                - 班级信息
                - 作业提交情况（已提交/未提交）
                - 学习进度（课程节点学习状态）
                - 考核成绩
                """;
    }

    private String getAnalysisContext() {
        return """
                数据分析包括以下信息：
                - 学习进度快照（各课程学习进度百分比）
                - 风险预警（学习风险、预警类型、预警描述）
                - 考核成绩统计（平时成绩、最终考核）
                - 学情分析（学习趋势、掌握情况）
                """;
    }
}
