package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 19:11 2025/6/16
 */
@Data
@Schema(description = "答案评估结果")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerEvaluationDTO {

    @Schema(description = "问题")
    private String question;

    @Schema(description = "分数")
    private Integer score;

    @Schema(description = "学生答案")
    private String studentAnswer;

    @Schema(description = "分析结果")
    private AnalysisDTO analysis;

    @Schema(description = "建议")
    private List<String> suggestions;

    @Schema(description = "亮点")
    private List<String> strengths;

    @Schema(description = "不足")
    private List<String> weaknesses;

    public Integer getScore() {
        return analysis.getInnovationScore()
                + analysis.getEngineeringAbilityScore()
                + analysis.getKnowledgeApplicationScore()
                + analysis.getMathematicalUnderstandingScore()
                + analysis.getProblemAnalysisScore();
    }
}
