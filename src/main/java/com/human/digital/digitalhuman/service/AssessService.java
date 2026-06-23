package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.CourseAssessStateEnums;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.AICallException;
import com.human.digital.digitalhuman.common.exception.AssessedException;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.ThreadPoolUtils;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessDetailPO;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessPO;
import com.human.digital.digitalhuman.repository.service.CourseNodeService;
import com.human.digital.digitalhuman.repository.service.CourseService;
import com.human.digital.digitalhuman.repository.service.StudentCourseAssessDetailService;
import com.human.digital.digitalhuman.repository.service.StudentCourseAssessService;
import com.human.digital.digitalhuman.service.manager.QuestionAndAnswerAnalysisManager;
import com.human.digital.digitalhuman.service.model.dto.AiQuestionDTO;
import com.human.digital.digitalhuman.service.model.dto.AnalysisDTO;
import com.human.digital.digitalhuman.service.model.dto.QuestionDTO;
import com.human.digital.digitalhuman.service.model.request.AnswerSaveCmd;
import com.human.digital.digitalhuman.service.model.request.AssessEndCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @USER taoHouChao
 * @DATE 15:02 2025/6/16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssessService {

    private static final String QUESTION_ONE = """
            作为大学考核专家，请根据以下课程视频内容设计一个考核题目：
            
            **课程内容摘要**:
            %s
            
            **要求**:
            1. 题目类型：开放式问题，考察对核心概念的理解
            2. 难度：中等偏上，需要综合应用课程知识点
            3. 内容：聚焦视频中的关键知识点
            4. 格式：
               - 题目清晰明确，不超过50字
            """;

    private static final String QUESTION_FOLLOW = """
            作为大学考核专家，请根据以下课程视频内容设计一个考核题目，需要排除掉已经考核过的题目：
            
            **课程内容摘要**:
            %s
            
            **已经考核题目**:
            [%s]
            
            **要求**:
            1. 题目类型：开放式问题，考察对核心概念的理解
            2. 难度：中等偏上，需要综合应用课程知识点
            3. 内容：聚焦视频中的关键知识点
            4. 格式：
               - 题目清晰明确，不超过50字
            """;

    private static final String QUESTION_FOLLOW_FINAL = """
            作为大学考核专家，请根据以下课程视频内容设计一个考核题目，在所有历史问答中选择一个题目进行更深层次的提问：
            
            **课程内容摘要**:
            %s
            
            **历史问答**:
            %s
            
            **已经考核题目**:
            %s
            
            **要求**:
            1. 题目类型：开放式问题，考察对核心概念的理解
            2. 要点：问题要与学生的回答紧密相关，避免重复原始问题，目标是挖掘其深层次理解，如果已经考核的题目不为空，则需要排除掉已经考核过的题目
            3. 难度：偏上，需要综合应用课程知识点
            4. 内容：聚焦视频中的关键知识点
            5. 格式：
               - 题目清晰明确，不超过50字
            """;

    private final ChatService chatService;

    private final ChatClient client;

    private final CourseService courseService;

    private final CourseNodeService courseNodeService;

    private final StudentCourseAssessService studentCourseAssessService;

    private final StudentCourseAssessDetailService studentCourseAssessDetailService;

    private final QuestionAndAnswerAnalysisManager questionAndAnswerAnalysisManager;

    private final RedisTemplate<String, Object> redisTemplate;

    public void analysisAssessDetail(){
        log.info("start repeat analysis");
        Map<Integer, CourseNodePO> courseNodeIdMap = courseNodeService.list().stream()
                .collect(Collectors.toMap(CourseNodePO::getId, courseNodePO -> courseNodePO));
        Map<Integer, List<StudentCourseAssessDetailPO>> detailNodeId = studentCourseAssessDetailService.list().stream()
                .collect(Collectors.groupingBy(StudentCourseAssessDetailPO::getCourseNodeId));
        detailNodeId.forEach((courseNodeId, courseNodeDetail) -> {
            CourseNodePO courseNodePO = courseNodeIdMap.get(courseNodeId);
            if (courseNodePO == null) {
                return;
            }
            Integer questionCount = courseNodePO.getQuestionCount();
            if (questionCount == null || questionCount <= 0) {
                return;
            }
            int totalScore = 100 / questionCount;
            for (StudentCourseAssessDetailPO item : courseNodeDetail) {
                String analysis = item.getAnalysis();
                if (StringUtils.isBlank(analysis)) {
                    if (Objects.equals(item.getState(), -1)) {
                        log.info("analysis fail node:{}", item);
                        ThreadPoolUtils.execute(() -> questionAndAnswerAnalysisManager.analysisAnswer(totalScore, item.getQuestion(), item.getAnswer(), courseNodePO.getContent(), item.getId()));
                    }
                }else {
                    AnalysisDTO analysisDTO = JSONUtil.toBean(item.getAnalysis(), AnalysisDTO.class);
                    int score = analysisDTO.getMathematicalUnderstandingScore() + analysisDTO.getKnowledgeApplicationScore() + analysisDTO.getProblemAnalysisScore() + analysisDTO.getEngineeringAbilityScore() + analysisDTO.getInnovationScore();
                    if (score > totalScore) {
                        log.info("analysis failed, detailId:{}, totalScore:{}, score:{}", item.getId(), totalScore, score);
                        ThreadPoolUtils.execute(() -> questionAndAnswerAnalysisManager.analysisAnswer(totalScore, item.getQuestion(), item.getAnswer(), courseNodePO.getContent(), item.getId()));
                    }
                }
            }
        });
    }

    /**
     * 根据课程节点ID和学生ID生成问题
     *
     * @param courseNodeId 课程节点ID
     * @param studentId    学生ID
     * @return 返回一个包含问题详情的对象
     */
    @Retryable(retryFor = AICallException.class)
    public QuestionDTO generateQuestion(Integer courseNodeId, Integer studentId) {
        // 获取课程节点信息
        CourseNodePO courseNode = courseNodeService.getById(courseNodeId);
        // 检查课程节点是否存在
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }
        // 获取课程节点内容
        String content = courseNode.getContent();
        // 查询学生对此课程节点的考核详情
        List<StudentCourseAssessDetailPO> assessDetails = studentCourseAssessDetailService.queryCourseNodeAssessing(courseNode.getCourseId(), courseNodeId, studentId);
        // 获取课程节点总问题数
        Integer totalQuestion = courseNode.getQuestionCount();
        if (assessDetails.size() == totalQuestion) {
            throw new AssessedException(ErrorCodeEnums.ASSESS_ALREADY_FINISH);
        }
        // 如果没有评估详情，生成第一个问题
        if (CollectionUtil.isEmpty(assessDetails)) {
            String question = String.format(QUESTION_ONE, content);
            AiQuestionDTO aiQuestion = client.prompt()
                    .user(question)
                    .call().entity(AiQuestionDTO.class);
            if (aiQuestion == null) {
                throw new AICallException(ErrorCodeEnums.AI_CALL_ERROR);
            }
            log.info("first question:{}", aiQuestion);
            return new QuestionDTO(1, totalQuestion, aiQuestion.content(), assessDetails.size() + 1 == totalQuestion);
        }
        try {
            // 如果有评估详情，生成后续问题
            String existTitle = assessDetails.stream().map(StudentCourseAssessDetailPO::getQuestion).collect(Collectors.joining(";"));
            String prompt = String.format(QUESTION_FOLLOW, content, existTitle);
            AiQuestionDTO aiQuestion = client.prompt()
                    .user(prompt)
                    .call().entity(AiQuestionDTO.class);
            if (aiQuestion == null) {
                throw new AICallException(ErrorCodeEnums.AI_CALL_ERROR);
            }
            log.info("first question:{}", aiQuestion);
            return new QuestionDTO(assessDetails.size() + 1, totalQuestion, aiQuestion.content(), assessDetails.size() + 1 == totalQuestion);
        } catch (Exception e) {
            log.error("generateQuestion error", e);
            throw new AICallException(ErrorCodeEnums.AI_CALL_ERROR);
        }
    }

    /**
     * 生成课程考核问题
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return  问题
     */
    @Retryable(retryFor = AICallException.class)
    public QuestionDTO generateFinalQuestion(Integer courseId, Integer studentId) {
        CoursePO course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        if (!course.getState()) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_START);
        }
        // 如果考核的题目数等于设定的考核题目数，则退出
        Integer totalTitle = course.getLastDefenseQuestionsCount();
        List<StudentCourseAssessDetailPO> finalAssessDetails = studentCourseAssessDetailService.queryCourseAssessing(courseId, studentId);
        if (finalAssessDetails.size() == totalTitle) {
            throw new AssessedException(ErrorCodeEnums.ASSESS_ALREADY_FINISH);
        }
        // 从所有考核中随机选则一个节点进行更深入的考核
        List<StudentCourseAssessDetailPO> assessDetails = studentCourseAssessDetailService.list(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .gt(StudentCourseAssessDetailPO::getCourseNodeId, -1)
                .eq(StudentCourseAssessDetailPO::getState, 1)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId));
        if (CollectionUtil.isEmpty(assessDetails)) {
            throw new BusinessException(ErrorCodeEnums.LEARNING_RECORD_NOT_EXIST);
        }
        StudentCourseAssessDetailPO matchAssessDetail = assessDetails.get(new Random().nextInt(assessDetails.size()));
        Integer courseNodeId = matchAssessDetail.getCourseNodeId();
        CourseNodePO courseNodePO = courseNodeService.getById(courseNodeId);
        if (courseNodePO == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }

        List<StudentCourseAssessDetailPO> assessNodeDetails = assessDetails.stream().filter(item -> Objects.equals(item.getCourseNodeId(), courseNodePO.getId())).toList();
        // 对原有的题目进行追加问题
        StringBuilder historyQuestion = new StringBuilder();
        for (StudentCourseAssessDetailPO assessDetail : assessNodeDetails) {
            historyQuestion.append("问题：").append(assessDetail.getQuestion()).append("\n");
            historyQuestion.append("回答：").append(assessDetail.getAnswer()).append("\n");
        }
        // 终极考核的问答，为了防止重复，需要排除掉
        String existTitle = finalAssessDetails.stream().map(StudentCourseAssessDetailPO::getQuestion).collect(Collectors.joining(";"));
        String prompt = String.format(QUESTION_FOLLOW_FINAL, courseNodePO.getContent(), historyQuestion, existTitle);
        AiQuestionDTO aiQuestion = client.prompt()
                .user(prompt)
                .call().entity(AiQuestionDTO.class);
        if (aiQuestion == null) {
            throw new AICallException(ErrorCodeEnums.AI_CALL_ERROR);
        }

        QuestionDTO question = new QuestionDTO(finalAssessDetails.size() + 1, totalTitle, aiQuestion.content(), finalAssessDetails.size() + 1 == totalTitle);
        // 保存 问题
        StudentCourseAssessDetailPO detailPO = StudentCourseAssessDetailPO.builder()
                .studentId(studentId)
                .courseId(courseId)
                .content(courseNodePO.getContent())
                .courseNodeId(-1)
                .question(question.getIndex() + "." + question.getTitle())
                .build();
        redisTemplate.opsForValue().set("courseAssess:" + studentId + ":" + courseId, detailPO, 60 * 60 * 24, TimeUnit.SECONDS);
        return question;
    }

    public Boolean saveAnswer(AnswerSaveCmd saveCmd) {
        int studentId = StpUtil.getLoginIdAsInt();
        Integer courseNodeId = saveCmd.getCourseNodeId();
        Integer courseId = saveCmd.getCourseId();
        String question = convertQuestion(saveCmd.getQuestion());
        boolean success;
        String content;
        int updateId;
        int questionScore;
        // 终极考核
        if (saveCmd.finalAssess()) {
            StudentCourseAssessDetailPO detailPO = (StudentCourseAssessDetailPO) redisTemplate.opsForValue().get("courseAssess:" + studentId + ":" + courseId);
            if (detailPO == null) {
                throw new BusinessException(ErrorCodeEnums.COURSE_ASSESS_NOT_EXIST);
            }

            detailPO.setAnswer(saveCmd.getAnswer());
            detailPO.setAnalysisState(0);
            detailPO.setState(CourseAssessStateEnums.ASSESSING.getCode());
            success = studentCourseAssessDetailService.save(detailPO);
            content = detailPO.getContent();
            updateId = detailPO.getId();
            // 删除缓存
            redisTemplate.delete("courseAssess:" + studentId + ":" + courseId);
            // 如果问题已经问答完成，则生成考核结果
            Integer lastDefenseQuestionsCount = courseService.getById(courseId).getLastDefenseQuestionsCount();
            questionScore = 100 / lastDefenseQuestionsCount;
        }
        // 普通考核
        else if (saveCmd.nodeAssess()) {
            CourseNodePO courseNode = courseNodeService.getById(courseNodeId);
            if (courseNode == null) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
            }
            StudentCourseAssessDetailPO detailPO = StudentCourseAssessDetailPO.builder()
                    .courseId(courseNode.getCourseId())
                    .courseNodeId(courseNodeId)
                    .studentId(studentId)
                    .question(question)
                    .analysisState(0)
                    .answer(saveCmd.getAnswer())
                    .state(CourseAssessStateEnums.ASSESSING.getCode())
                    .build();
            success = studentCourseAssessDetailService.save(detailPO);
            content = courseNode.getContent();
            updateId = detailPO.getId();
            // 如果问题已经问答完成，则生成考核结果
            Integer questionCount = courseNode.getQuestionCount();
            questionScore = 100 / questionCount;
        } else {
            success = false;
            content = "";
            updateId = -1;
            questionScore = 10;
        }
        if (success) {
            ThreadPoolUtils.execute(() -> questionAndAnswerAnalysisManager.analysisAnswer(questionScore, question, saveCmd.getAnswer(), content, updateId));
        }
        return true;
    }

    public Boolean endAssess(Integer studentId, AssessEndCmd endCmd) {
        // 终极考核结束
        Boolean noQuestionEnd = endCmd.getNoQuestionEnd();
        if (Objects.equals(noQuestionEnd, Boolean.TRUE)) {
            if (Objects.nonNull(endCmd.getCourseId()) && Objects.isNull(endCmd.getCourseNodeId())) {
                CoursePO course = courseService.getById(endCmd.getCourseId());
                if (Objects.isNull(course)) {
                    throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
                }
                StudentCourseAssessPO courseAssess = studentCourseAssessService.getOne(Wrappers.lambdaQuery(StudentCourseAssessPO.class)
                        .eq(StudentCourseAssessPO::getCourseId, endCmd.getCourseId())
                        .eq(StudentCourseAssessPO::getStudentId, studentId)
                        .eq(StudentCourseAssessPO::getCourseNodeId, -1));
                if (Objects.nonNull(courseAssess)) {
                    courseAssess.setNoQuestion(true);
                    courseAssess.setUpdateTime(LocalDateTime.now());
                    studentCourseAssessService.updateById(courseAssess);
                }else {
                    StudentCourseAssessPO assessPO = new StudentCourseAssessPO();
                    assessPO.setStudentId(studentId);
                    assessPO.setCourseId(endCmd.getCourseId());
                    assessPO.setCourseNodeId(-1);
                    assessPO.setAssessMode(2);
                    assessPO.setNoQuestion(true);
                    studentCourseAssessService.save(assessPO);
                }
                updateAssessDetailState(course.getId(), -1, studentId);
            }
            // 节点考核
            else {
                CourseNodePO courseNode = courseNodeService.getById(endCmd.getCourseNodeId());
                if (courseNode == null) {
                    throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
                }
                StudentCourseAssessPO studentCourseAssessPO = studentCourseAssessService.findCourseNodeAssess(courseNode.getId(), studentId);
                if (Objects.nonNull(studentCourseAssessPO)) {
                    studentCourseAssessPO.setNoQuestion(true);
                    studentCourseAssessPO.setUpdateTime(LocalDateTime.now());
                    studentCourseAssessService.updateById(studentCourseAssessPO);
                }else {
                    StudentCourseAssessPO assessPO = new StudentCourseAssessPO();
                    assessPO.setNoQuestion(true);
                    assessPO.setStudentId(studentId);
                    assessPO.setCourseId(courseNode.getCourseId());
                    assessPO.setCourseNodeId(courseNode.getId());
                    assessPO.setAssessMode(1);
                    studentCourseAssessService.save(assessPO);
                }
                updateAssessDetailState(courseNode.getCourseId(), courseNode.getId(), studentId);
            }
            return true;
        }
        if (Objects.nonNull(endCmd.getCourseId()) && Objects.isNull(endCmd.getCourseNodeId())) {
            CoursePO course = courseService.getById(endCmd.getCourseId());
            if (Objects.isNull(course)) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
            }
            List<StudentCourseAssessDetailPO> courseAssessDetails = studentCourseAssessDetailService.queryCourseAssessing(endCmd.getCourseId(), studentId);
            StudentCourseAssessPO courseAssess = studentCourseAssessService.getOne(Wrappers.lambdaQuery(StudentCourseAssessPO.class)
                    .eq(StudentCourseAssessPO::getCourseId, endCmd.getCourseId())
                    .eq(StudentCourseAssessPO::getStudentId, studentId)
                    .eq(StudentCourseAssessPO::getCourseNodeId, -1));
            if (Objects.nonNull(courseAssess)) {
                courseAssess.setAssessScore(courseAssessDetails.stream().mapToInt(StudentCourseAssessDetailPO::getScore).sum());
                courseAssess.setUpdateTime(LocalDateTime.now());
                studentCourseAssessService.updateById(courseAssess);
            }else {
                StudentCourseAssessPO assessPO = new StudentCourseAssessPO();
                assessPO.setStudentId(studentId);
                assessPO.setCourseId(endCmd.getCourseId());
                assessPO.setCourseNodeId(-1);
                assessPO.setAssessScore(courseAssessDetails.stream().mapToInt(StudentCourseAssessDetailPO::getScore).sum());
                assessPO.setAssessMode(2);
                studentCourseAssessService.save(assessPO);
            }
            updateAssessDetailState(course.getId(), -1, studentId);
        }
        // 节点考核
        else {
            CourseNodePO courseNode = courseNodeService.getById(endCmd.getCourseNodeId());
            if (courseNode == null) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
            }
            List<StudentCourseAssessDetailPO> assessDetail = studentCourseAssessDetailService.queryCourseNodeAssessing(courseNode.getCourseId(), courseNode.getId(), studentId);
            if (assessDetail.isEmpty()) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_ANSWER);
            }
            // 总分数
            int score = assessDetail.stream().mapToInt(StudentCourseAssessDetailPO::getScore).sum();
            StudentCourseAssessPO studentCourseAssessPO = studentCourseAssessService.findCourseNodeAssess(courseNode.getId(), studentId);
            if (Objects.nonNull(studentCourseAssessPO)) {
                studentCourseAssessPO.setAssessScore(score);
                studentCourseAssessPO.setUpdateTime(LocalDateTime.now());
                studentCourseAssessService.updateById(studentCourseAssessPO);
            }else {
                StudentCourseAssessPO assessPO = new StudentCourseAssessPO();
                assessPO.setStudentId(studentId);
                assessPO.setCourseId(courseNode.getCourseId());
                assessPO.setCourseNodeId(courseNode.getId());
                assessPO.setAssessScore(score);
                assessPO.setAssessMode(1);
                studentCourseAssessService.save(assessPO);
            }
            updateAssessDetailState(courseNode.getCourseId(), courseNode.getId(), studentId);
        }
        return true;
    }

    private void updateAssessDetailState(Integer courseId, Integer courseNodeId, Integer studentId) {
        // 将最新的更新为历史的
        studentCourseAssessDetailService.update(Wrappers.lambdaUpdate(StudentCourseAssessDetailPO.class)
                .set(StudentCourseAssessDetailPO::getState, 0)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, 1));
        // 将考核中的更新为最新的
        studentCourseAssessDetailService.update(Wrappers.lambdaUpdate(StudentCourseAssessDetailPO.class)
                .set(StudentCourseAssessDetailPO::getState, 1)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, -1));
    }

    public Boolean checkAbleEndAssess(AssessEndCmd endCmd) {
        int studentId = StpUtil.getLoginIdAsInt();
        if (Objects.nonNull(endCmd.getCourseId()) && Objects.isNull(endCmd.getCourseNodeId())) {
            CoursePO course = courseService.getById(endCmd.getCourseId());
            if (Objects.isNull(course)) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
            }
            List<StudentCourseAssessDetailPO> courseAssessDetails = studentCourseAssessDetailService.queryCourseAssessing(endCmd.getCourseId(), studentId);
            return retryAnalysis(courseAssessDetails);
        } else {
            CourseNodePO courseNode = courseNodeService.getById(endCmd.getCourseNodeId());
            if (courseNode == null) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
            }
            List<StudentCourseAssessDetailPO> assessDetail = studentCourseAssessDetailService.queryCourseNodeAssessing(courseNode.getCourseId(), courseNode.getId(), studentId);
            return retryAnalysis(assessDetail);
        }
    }

    private boolean retryAnalysis(List<StudentCourseAssessDetailPO> courseAssessDetails) {
        List<StudentCourseAssessDetailPO> notReadyAssessDetail = courseAssessDetails.stream().filter(item -> item.getAnalysisState().equals(-1)).toList();
        if (CollectionUtil.isNotEmpty(notReadyAssessDetail)) {
            for (StudentCourseAssessDetailPO studentCourseAssessDetailPO : notReadyAssessDetail) {
                ThreadPoolUtils.execute(() -> questionAndAnswerAnalysisManager.analysisAnswer(100 / courseAssessDetails.size(), studentCourseAssessDetailPO.getQuestion(), studentCourseAssessDetailPO.getAnswer(), studentCourseAssessDetailPO.getContent(), studentCourseAssessDetailPO.getId()));
            }
            return false;
        }
        boolean analysis = courseAssessDetails.stream().anyMatch(item -> item.getAnalysisState() == 0);
        // 如果有分析中的，则需要稍等
        return !analysis;
    }

    private String convertQuestion(QuestionDTO question) {
        return question.getIndex() + "." + question.getTitle();
    }

    public Boolean nodeStartAssess(Integer courseNodeId, Integer studentId) {
        studentCourseAssessDetailService.remove(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, -1));
        return true;
    }

    public Boolean startFinalAssess(Integer courseId, Integer studentId) {
        studentCourseAssessDetailService.remove(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, -1)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, -1));
        return true;
    }
}
