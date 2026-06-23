package com.human.digital.digitalhuman.service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 16:19 2025/6/23
 */
@Data
@Schema(description = "答案评估结果", requiredProperties = {"analysis", "suggestions", "strengths", "weaknesses"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerEvaluationSimpleDTO {

    @Schema(description = "分析结果")
    private AnalysisDTO analysis;

    @Schema(description = "建议")
    private List<String> suggestions;

    @Schema(description = "亮点")
    private List<String> strengths;

    @Schema(description = "不足")
    private List<String> weaknesses;

    public Integer getScore() {
        if (analysis != null) {
            return analysis.getInnovationScore()
                    + analysis.getEngineeringAbilityScore()
                    + analysis.getKnowledgeApplicationScore()
                    + analysis.getMathematicalUnderstandingScore()
                    + analysis.getProblemAnalysisScore();
        }
        return 0;
    }
}
