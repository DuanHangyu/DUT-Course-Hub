package com.human.digital.digitalhuman.service.model.response;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.human.digital.digitalhuman.service.model.dto.AnalysisDTO;
import com.human.digital.digitalhuman.service.model.dto.AnswerEvaluationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 14:42 2025/6/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核结果详情")
public class AssessmentResultDetailDTO {

    @Schema(description = "考核结果ID")
    private Integer id;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "及格线")
    private Integer passLine;

    @Schema(description = "教师审核得分")
    @JsonIgnore
    private Integer teacherCheckScore;

    @Schema(description = "问题答案评估结果")
    private List<AnswerEvaluationDTO> questionAndAnswers;

    @Schema(description = "分析结果")
    private AssessAnalysis analysis;

    public Integer getScore() {
        if (teacherCheckScore != null && teacherCheckScore > 0) {
            return teacherCheckScore;
        }
        if (CollectionUtil.isNotEmpty(questionAndAnswers)) {
            return questionAndAnswers.stream().mapToInt(AnswerEvaluationDTO::getScore).sum();
        }
        return score;
    }

    public AssessAnalysis getAnalysis() {
        if (this.analysis == null && CollectionUtil.isNotEmpty(questionAndAnswers)) {
            int mathematicalUnderstandingScore = 0,
                    knowledgeApplicationScore = 0,
                    problemAnalysisScore = 0,
                    engineeringAbilityScore = 0,
                    innovationScore = 0;
            for (AnswerEvaluationDTO questionAndAnswer : this.questionAndAnswers) {
                AnalysisDTO analysisDTO = questionAndAnswer.getAnalysis();
                mathematicalUnderstandingScore += analysisDTO.getMathematicalUnderstandingScore();
                knowledgeApplicationScore += analysisDTO.getKnowledgeApplicationScore();
                problemAnalysisScore += analysisDTO.getProblemAnalysisScore();
                engineeringAbilityScore += analysisDTO.getEngineeringAbilityScore();
                innovationScore += analysisDTO.getInnovationScore();
            }
            this.analysis = AssessAnalysis.builder()
                    .mathematicalUnderstandingScore(mathematicalUnderstandingScore)
                    .knowledgeApplicationScore(knowledgeApplicationScore)
                    .problemAnalysisScore(problemAnalysisScore)
                    .engineeringAbilityScore(engineeringAbilityScore)
                    .innovationScore(innovationScore)
                    .build();
        }
        return analysis;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "考核评估结果")
    public static class AssessAnalysis{

        @Schema(description = "数学理解分数")
        private Integer mathematicalUnderstandingScore;

        @Schema(description = "知识应用分数")
        private Integer knowledgeApplicationScore;

        @Schema(description = "问题分析分数")
        private Integer problemAnalysisScore;

        @Schema(description = "工程能力分数")
        private Integer engineeringAbilityScore;

        @Schema(description = "拓展能力分数")
        private Integer innovationScore;
    }
}
