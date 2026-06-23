package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.repository.service.*;
import com.human.digital.digitalhuman.service.manager.StudentAssessManager;
import com.human.digital.digitalhuman.service.model.dto.AnalysisDTO;
import com.human.digital.digitalhuman.service.model.dto.AnswerEvaluationDTO;
import com.human.digital.digitalhuman.service.model.dto.AnswerEvaluationSimpleDTO;
import com.human.digital.digitalhuman.service.model.request.AssessmentResultListQuery;
import com.human.digital.digitalhuman.service.model.request.LearnRecordListQuery;
import com.human.digital.digitalhuman.service.model.request.LearningRecordModifyDTO;
import com.human.digital.digitalhuman.service.model.response.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @USER taoHouChao
 * @DATE 10:53 2025/6/15
 */
@Service
@RequiredArgsConstructor
public class StudentAssessAppService {

    private static final String analysis_temp = """
            作为大学考核专家，请根据课程内容,考核题目和学生针对考核题目做出的回答进行评估：
            
                        **课程内容摘要**:
                        {content}
            
                        **考核题目**:
                        {question}
            
                        **学生回答**:
                        {answer}
            
                        **评估维度**:
                        1. 数学原理理解（百分之20）
                        2. 知识运用能力（百分之25）
                        3. 问题分析能力（百分之20）
                        4. 工程实现能力（百分之20）
                        5. 拓展创新能力（百分之15）
            
                        **评分规则**
                        1. 总分：{totalScore}
                        2. 数学原理理解满分：总分 * 0.2
                        3. 知识运用能力满分：总分 * 0.25
                        4. 问题分析能力满分：总分 * 0.2
                        5. 工程实现能力满分：总分 * 0.2
                        6. 拓展创新能力满分：总分 * 0.15
                        7. 所有评分均为整数，采用「扣分制」：从满分开始，按缺失点扣分。
                        8. 所有维度的评分总和不能超过总分
            
                        **输出前请务必自行计算并校验**：
                        - 每个维度得分是否 ≤ 其满分？
                        - 所有维度得分总和是否 ≤ 总分？
                        → 如不满足，请重新调整评分！
            
                        **输出要求**:
                        1. 分析：针对每个维度给出具体评语，并根据上述的评分规则给出评分
                        2. 建议：提供可操作的改进建议
                        3. 亮点与不足：客观指出优点和缺点
            """;

    private final StudentCourseAssessService studentCourseAssessService;

    private final StudentCourseAssessDetailService studentCourseAssessDetailService;

    private final StudentService studentService;

    private final CourseService courseService;

    private final CourseNodeService courseNodeService;

    private final CourseStudentRelationService studentRelationService;

    private final ChatClient chatClient;

    private final StudentAssessManager studentAssessManager;

    private static final IPage<AssessmentResultSummaryDTO> EMPTY_PAGE = new Page<>();

    @Transactional(readOnly = true)
    public IPage<LearnRecordSummaryDTO> learnRecordList(LearnRecordListQuery listQuery) {
        List<Integer> manageStudentIds = queryManageStudent();
        if (CollectionUtils.isEmpty(manageStudentIds)) {
            return new Page<>();
        }
        IPage<StudentPO> studentPage = studentService.page(new Page<>(listQuery.getPage(), listQuery.getSize()), Wrappers.lambdaQuery(StudentPO.class)
                .like(StringUtils.isNotBlank(listQuery.getStudentName()), StudentPO::getStudentName, listQuery.getStudentName())
                .in(StudentPO::getId, manageStudentIds)
                .orderByAsc(StudentPO::getId));
        List<StudentPO> studentRecords = studentPage.getRecords();
        if (CollectionUtils.isEmpty(studentRecords)) {
            return new Page<>();
        }
        List<Integer> studentIds = studentRecords.stream().map(StudentPO::getId).toList();
        List<CourseStudentRelationPO> relations = studentRelationService.queryByStudentIds(studentIds);
        Map<Integer, List<CourseStudentRelationPO>> studentRelationMap = relations.stream().collect(Collectors.groupingBy(CourseStudentRelationPO::getStudentId));

        List<Integer> courseIds = new ArrayList<>(relations.stream().map(CourseStudentRelationPO::getCourseId).distinct().toList());
        // 防止下面的查询报错
        if (CollectionUtils.isEmpty(courseIds)) {
            courseIds.add(-1);
        }
        List<CoursePO> courses = courseService.listByIds(courseIds);
        Map<Integer, CoursePO> courseIdMap = courses.stream().collect(Collectors.toMap(CoursePO::getId, item -> item));

        List<StudentCourseAssessPO> courseAssessList = studentCourseAssessService.queryByStudentIds(studentIds);
        courseAssessList.removeIf(assess -> !courseIdMap.containsKey(assess.getCourseId()));
        Map<Integer, List<StudentCourseAssessPO>> courseAssessMap = courseAssessList.stream().collect(Collectors.groupingBy(StudentCourseAssessPO::getStudentId));

        return studentPage.convert(item -> convertFromPO(item, courseIdMap,
                studentRelationMap.getOrDefault(item.getId(), Collections.emptyList()).stream().distinct().toList(),
                courseAssessMap.getOrDefault(item.getId(), Collections.emptyList())));
    }

    private List<Integer> queryManageStudent() {
        int teacherId = StpUtil.getLoginIdAsInt();
        // 获取管理的课程
        IPage<CoursePO> coursePOIPage = courseService.pageQuery(teacherId, 1, 1000, null, null);
        List<CoursePO> records = coursePOIPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return List.of(-1);
        }
        List<Integer> courseIds = records.stream().map(CoursePO::getId).distinct().toList();
        List<CourseStudentRelationPO> studentRelations = studentRelationService.queryByCourseIds(courseIds);
        return studentRelations.stream().map(CourseStudentRelationPO::getStudentId).distinct().toList();
    }

    private LearnRecordSummaryDTO convertFromPO(StudentPO studentPO,
                                                Map<Integer, CoursePO> courseIdMap,
                                                List<CourseStudentRelationPO> partitionCourse,
                                                List<StudentCourseAssessPO> assessList) {
        // 剔除脏数据
        Set<Integer> courseIdSet = partitionCourse.stream().map(CourseStudentRelationPO::getCourseId).collect(Collectors.toSet());
        assessList.removeIf(item -> !courseIdSet.contains(item.getCourseId()));

        int participateCourseCount = partitionCourse.size();
        Map<Integer, List<StudentCourseAssessPO>> courseIdAssessMap = assessList.stream().collect(Collectors.groupingBy(StudentCourseAssessPO::getCourseId));
        List<LearnRecordSummaryDTO.CourseDTO> participateCourseList = partitionCourse.stream()
                .map(CourseStudentRelationPO::getCourseId)
                .map(item -> convertToCourseDTO(courseIdMap, item, courseIdAssessMap)).toList();

        List<Integer> completeCourseIds = assessList.stream()
                .filter(item -> Objects.equals(item.getAssessMode(), 2))
                .map(StudentCourseAssessPO::getCourseId)
                .distinct()
                .toList();
        List<LearnRecordSummaryDTO.CourseDTO> completeCourse = completeCourseIds.stream()
                .map(item -> convertToCourseDTO(courseIdMap, item, courseIdAssessMap))
                .toList();

        List<LearnRecordSummaryDTO.CourseDTO> waitStudyCourseList = partitionCourse.stream()
                .map(CourseStudentRelationPO::getCourseId)
                .filter(courseId -> !completeCourseIds.contains(courseId))
                .map(item -> convertToCourseDTO(courseIdMap, item, courseIdAssessMap)).toList();

        Optional<StudentCourseAssessPO> first = assessList.stream().max(Comparator.comparing(StudentCourseAssessPO::getCreateTime));
        int alreadyCompleteCourseCount = (int) assessList.stream().filter(item -> Objects.equals(item.getAssessMode(), 2)).count();
        return LearnRecordSummaryDTO.builder()
                .studentId(studentPO.getId())
                .studentName(studentPO.getStudentName())
                .idNumber(studentPO.getIdNumber())
                .schoolClass(studentPO.getSchoolClass())
                .participateCourseCount(participateCourseCount)
                .participateCourseList(participateCourseList)
                .alreadyCompleteCourseCount(alreadyCompleteCourseCount)
                .alreadyCompleteCourseList(completeCourse)
                .waitStudyCourseCount(Math.max(0, participateCourseCount - alreadyCompleteCourseCount))
                .waitStudyCourseList(waitStudyCourseList)
                .lastStudyTime(first.map(StudentCourseAssessPO::getCreateTime).orElse(null))
                .build();
    }

    private static LearnRecordSummaryDTO.CourseDTO convertToCourseDTO(Map<Integer, CoursePO> courseIdMap, Integer item, Map<Integer, List<StudentCourseAssessPO>> courseIdAssessMap) {
        CoursePO coursePO = courseIdMap.get(item);
        List<StudentCourseAssessPO> courseAssessList = courseIdAssessMap.getOrDefault(item, Collections.emptyList());
        LocalDateTime lastAssessmentTime = courseAssessList.stream()
                .max(Comparator.comparing(StudentCourseAssessPO::getCreateTime))
                .map(StudentCourseAssessPO::getCreateTime)
                .orElse(null);
        return LearnRecordSummaryDTO.CourseDTO.builder()
                .courseName(coursePO == null ? "" : coursePO.getCourseName())
                .lastAssessmentTime(lastAssessmentTime)
                .build();
    }

    public IPage<AssessmentResultSummaryDTO> list(AssessmentResultListQuery listQuery) {
        List<Integer> manageStudentIds = queryManageStudent();
        if (CollectionUtils.isEmpty(manageStudentIds)) {
            return new Page<>();
        }
        LambdaQueryWrapper<StudentCourseAssessPO> queryWrapper = Wrappers.lambdaQuery(StudentCourseAssessPO.class);
        queryWrapper.eq(listQuery.getAssessMode() != null, StudentCourseAssessPO::getAssessMode, listQuery.getAssessMode());
        queryWrapper.eq(StudentCourseAssessPO::getNoQuestion, false);
        queryWrapper.in(StudentCourseAssessPO::getStudentId, manageStudentIds);
        queryWrapper.orderByDesc(StudentCourseAssessPO::getCreateTime);
        if (StrUtil.isNotBlank(listQuery.getStudentName())) {
            List<Integer> studentIds = studentService.queryIdByName(listQuery.getStudentName());
            if (CollectionUtil.isEmpty(studentIds)) {
                return EMPTY_PAGE;
            }
            queryWrapper.in(StudentCourseAssessPO::getStudentId, studentIds);
        }
        if (StrUtil.isNotBlank(listQuery.getCourseName())) {
            List<Integer> courseIds = courseService.queryIdByName(listQuery.getCourseName());
            if (CollectionUtil.isEmpty(courseIds)) {
                return EMPTY_PAGE;
            }
            queryWrapper.in(StudentCourseAssessPO::getCourseId, courseIds);
        }
        if (StrUtil.isNotBlank(listQuery.getCourseNodeName())) {
            List<Integer> courseNodeIds = courseNodeService.queryIdByName(listQuery.getCourseNodeName());
            if (CollectionUtil.isEmpty(courseNodeIds)) {
                return EMPTY_PAGE;
            }
            queryWrapper.in(StudentCourseAssessPO::getCourseNodeId, courseNodeIds);
        }
        IPage<StudentCourseAssessPO> page = studentCourseAssessService.page(new Page<>(listQuery.getPage(), listQuery.getSize()), queryWrapper);
        List<StudentCourseAssessPO> records = page.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return EMPTY_PAGE;
        }
        List<Integer> studentIds = new ArrayList<>(records.size());
        List<Integer> courseIds = new ArrayList<>(records.size());
        List<Integer> courseNodeIds = new ArrayList<>(records.size());
        for (StudentCourseAssessPO record : records) {
            studentIds.add(record.getStudentId());
            courseIds.add(record.getCourseId());
            courseNodeIds.add(record.getCourseNodeId());
        }
        Map<Integer, StudentPO> studentIdMap = studentService.queryByIds(studentIds);
        Map<Integer, CoursePO> courseIdMap = courseService.queryByIds(courseIds);
        Map<Integer, CourseNodePO> courseNodeIdMap = courseNodeService.queryByIds(courseNodeIds);
        return page.convert(po -> AssessmentResultSummaryDTO.fromPO(po,
                studentId -> Optional.ofNullable(studentIdMap.get(studentId)).map(StudentPO::getStudentName).orElse(""),
                courseId -> Optional.ofNullable(courseIdMap.get(courseId)).map(CoursePO::getCourseName).orElse(""),
                courseNodeId -> Optional.ofNullable(courseNodeIdMap.get(courseNodeId)).map(CourseNodePO::getNodeName).orElse("")));
    }

    public AssessmentResultDetailDTO recordDetail(Integer id) {
        StudentCourseAssessPO assess = studentCourseAssessService.getById(id);
        if (assess == null) {
            throw new BusinessException(ErrorCodeEnums.LEARNING_RECORD_NOT_EXIST);
        }
        Integer passLine;
        if (assess.getAssessMode() == 1) {
            CourseNodePO courseNode = courseNodeService.getById(assess.getCourseNodeId());
            if (courseNode == null) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
            }
            passLine = courseNode.getPassLine();
        } else {
            passLine = 60;
        }
        List<StudentCourseAssessDetailPO> assessDetails = studentCourseAssessDetailService.queryCourseNodeAssess(assess.getCourseId(), assess.getCourseNodeId(), assess.getStudentId());

        Integer teacherCheckScore = assess.getTeacherCheckScore();
        return AssessmentResultDetailDTO.builder()
                .id(assess.getId())
                .score(teacherCheckScore != null && teacherCheckScore > 0 ? teacherCheckScore : assess.getAssessScore())
                .passLine(passLine)
                .teacherCheckScore(teacherCheckScore)
                .questionAndAnswers(assessDetails.stream()
                        .map(item -> AnswerEvaluationDTO.builder()
                                .question(item.getQuestion())
                                .studentAnswer(item.getAnswer())
                                .score(item.getScore())
                                .analysis(JSONUtil.toBean(item.getAnalysis(), AnalysisDTO.class))
                                .suggestions(JSONUtil.toList(item.getSuggestion(), String.class))
                                .strengths(JSONUtil.toList(item.getHighlights(), String.class))
                                .weaknesses(JSONUtil.toList(item.getShortcomings(), String.class))
                                .build()).toList())
                .build();
    }

    public AssessDetailDTO assessDetail(Integer courseNodeId) {
        int studentId = StpUtil.getLoginIdAsInt();
        StudentCourseAssessPO assess = studentCourseAssessService.findCourseNodeAssess(courseNodeId, studentId);
        if (assess == null) {
            throw new BusinessException(ErrorCodeEnums.LEARNING_RECORD_NOT_EXIST);
        }
        return AssessDetailDTO.of(recordDetail(assess.getId()));
    }

    public AssessDetailDTO courseAssessDetail(Integer courseId) {
        int studentId = StpUtil.getLoginIdAsInt();
        List<StudentCourseAssessPO> assessList = studentCourseAssessService.queryCourseAssess(courseId, studentId);
        StudentCourseAssessPO courseAssess = assessList.stream()
                .filter(item -> item.getAssessMode().equals(2))
                .findFirst().orElse(null);
        if (courseAssess == null) {
            throw new BusinessException(ErrorCodeEnums.LEARNING_RECORD_NOT_EXIST);
        }
        return AssessDetailDTO.of(recordDetail(courseAssess.getId()));
    }

    public Boolean modifyCheckScore(LearningRecordModifyDTO modifyDTO) {
        StudentCourseAssessPO courseAssess = studentCourseAssessService.getById(modifyDTO.getId());
        if (courseAssess == null) {
            throw new BusinessException(ErrorCodeEnums.LEARNING_RECORD_NOT_EXIST);
        }
        if (Objects.equals(courseAssess.getAssessMode(), 1)) {
            throw new BusinessException(ErrorCodeEnums.LEARNING_RECORD_NOT_LAST_NOE);
        }
        return studentCourseAssessService.update(Wrappers.lambdaUpdate(StudentCourseAssessPO.class)
                .set(StudentCourseAssessPO::getTeacherCheckScore, modifyDTO.getTeacherCheckScore())
                .eq(StudentCourseAssessPO::getId, modifyDTO.getId()));
    }

    public List<StudentCourseDetailDTO> studentCourseDetailList(Integer studentId) {
        List<CourseStudentRelationPO> relations = studentRelationService.queryByStudentId(studentId);
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        return relations.parallelStream()
                .map(relation -> studentAssessManager.buildFromRelation(studentId, relation))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public AnswerEvaluationSimpleDTO analysis(Integer assessDetailId) {
        StudentCourseAssessDetailPO detail = studentCourseAssessDetailService.getById(assessDetailId);
        if (detail == null) {
            throw new BusinessException(ErrorCodeEnums.ASSESS_DETAIL_NOT_EXIST);
        }
        Integer courseId = detail.getCourseId();
        CoursePO course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        CourseNodePO courseNode = courseNodeService.getById(detail.getCourseNodeId());
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_ASSESS_NOT_EXIST);
        }
        AnswerEvaluationSimpleDTO evaluationSimpleDTO = chatClient.prompt()
                .user(user -> user.text(analysis_temp)
                        .params(Map.of(
                                        "content", courseNode.getContent(),
                                        "question", detail.getQuestion(),
                                        "answer", detail.getAnswer(),
                                        "totalScore", 33
                                )
                        )
                )
                .call()
                .entity(AnswerEvaluationSimpleDTO.class);
        if (evaluationSimpleDTO == null) {
            throw new BusinessException(ErrorCodeEnums.GET_RESPONSE_ERROR);
        }
        Integer totalScore = evaluationSimpleDTO.getScore();
        if (totalScore > 33) {
            throw new BusinessException(ErrorCodeEnums.GET_RESPONSE_ERROR);
        }
        return evaluationSimpleDTO;
    }
}
