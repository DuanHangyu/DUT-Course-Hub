package com.human.digital.digitalhuman.service.manager;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessDetailPO;
import com.human.digital.digitalhuman.repository.service.StudentCourseAssessDetailService;
import com.human.digital.digitalhuman.service.model.dto.AnswerEvaluationSimpleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @USER taoHouChao
 * @DATE 21:20 2025/6/22
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionAndAnswerAnalysisManager {

    private static final String ANALYSIS = """
            作为大学考核专家，请根据课程内容,考核题目和学生针对考核题目做出的回答进行评估：
            
                        **课程内容摘要**:
                        {content}
            
                        **考核题目**:
                        {question}
            
                        **学生回答**:
                        {answer}
            
                        **评估维度及对应的满分**:
                        1. 数学原理理解：{mathematicalUnderstandingScore}
                        2. 知识运用能力：{knowledgeApplicationScore}
                        3. 问题分析能力：{problemAnalysisScore}
                        4. 工程实现能力：{engineeringAbilityScore}
                        5. 拓展创新能力：{innovationScore}
            
                        **评分规则**
                        1. 所有评分均为整数，采用「扣分制」：从满分开始，按缺失点扣分。
                        2. 所有维度的评分总和不能超过总分
            
                        **输出前请务必自行计算并校验**：
                        - 每个维度得分是否 ≤ 其满分？
                        - 所有维度得分总和是否 ≤ 总分？
                        → 如不满足，请重新调整评分！
            
                        **输出要求**:
                        1. 分析：针对每个维度给出具体评语，并根据上述的评分规则给出评分
                        2. 建议：提供可操作的改进建议
                        3. 亮点与不足：客观指出优点和缺点
            }""";

    private final ChatClient chatClient;

    private final StudentCourseAssessDetailService studentCourseAssessDetailService;

    @Retryable(retryFor = BusinessException.class, maxAttempts = 5)
    public void analysisAnswer(Integer questionScore, String question, String answer, String content, int updateId) {
        long startTime = System.currentTimeMillis();
        log.info("start analysis questionScore:{}, question:{}, answer:{}, content:{} time:{}", questionScore, question, answer, content, startTime);
        // 开始分析
        try {
            AnswerEvaluationSimpleDTO evaluation = chatClient.prompt()
                    .user(user -> user.text(ANALYSIS)
                            .params(Map.of(
                                            "content", content,
                                            "question", question,
                                            "answer", answer,
                                            "totalScore", questionScore,
                                            "mathematicalUnderstandingScore", (int) Math.floor(questionScore * 0.2),
                                            "knowledgeApplicationScore", (int) Math.floor(questionScore * 0.25),
                                            "problemAnalysisScore", (int) Math.floor(questionScore * 0.2),
                                            "engineeringAbilityScore", (int) Math.floor(questionScore * 0.2),
                                            "innovationScore", (int) Math.floor(questionScore * 0.15)
                                    )
                            )
                    )
                    .call()
                    .entity(AnswerEvaluationSimpleDTO.class);
            if (evaluation == null) {
                throw new BusinessException(ErrorCodeEnums.ANALYSIS_FAILED);
            }
            Integer totalScore = evaluation.getScore();
            log.info("evaluation:{}", evaluation);
            if (totalScore > questionScore) {
                throw new BusinessException(ErrorCodeEnums.ANALYSIS_FAILED);
            }
            long endTime = System.currentTimeMillis();
            log.info("end analysis time:{}, use time:{}", endTime, (endTime - startTime) / 1000);
            studentCourseAssessDetailService.update(Wrappers.lambdaUpdate(StudentCourseAssessDetailPO.class)
                    .set(StudentCourseAssessDetailPO::getScore, evaluation.getScore())
                    .set(StudentCourseAssessDetailPO::getAnalysis, JSONUtil.toJsonStr(evaluation.getAnalysis()))
                    .set(StudentCourseAssessDetailPO::getSuggestion, JSONUtil.toJsonStr(evaluation.getSuggestions()))
                    .set(StudentCourseAssessDetailPO::getHighlights, JSONUtil.toJsonStr(evaluation.getStrengths()))
                    .set(StudentCourseAssessDetailPO::getShortcomings, JSONUtil.toJsonStr(evaluation.getWeaknesses()))
                    .set(StudentCourseAssessDetailPO::getAnalysisState, 1)
                    .eq(StudentCourseAssessDetailPO::getId, updateId));
        } catch (Exception e) {
            log.error("分析失败", e);
            // 还原分析状态，避免下次无法再次分析
            studentCourseAssessDetailService.update(Wrappers.lambdaUpdate(StudentCourseAssessDetailPO.class)
                    .set(StudentCourseAssessDetailPO::getAnalysisState, -1)
                    .eq(StudentCourseAssessDetailPO::getId, updateId));
            throw new BusinessException(ErrorCodeEnums.ANALYSIS_FAILED);
        }
    }
}
