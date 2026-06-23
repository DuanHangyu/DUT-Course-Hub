package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 21:01 2025/6/16
 */
@Data
@Schema(description = "分析结果", requiredProperties = {"mathematicalUnderstanding",
        "mathematicalUnderstandingScore",
        "knowledgeApplication",
        "knowledgeApplicationScore",
        "problemAnalysis",
        "problemAnalysisScore",
        "engineeringAbility",
        "engineeringAbilityScore",
        "innovation",
        "innovationScore"})
public class AnalysisDTO {

    @Schema(description = "数学原理")
    private String mathematicalUnderstanding;

    @Schema(description = "数学理解分数")
    private Integer mathematicalUnderstandingScore;

    @Schema(description = "知识应用")
    private String knowledgeApplication;

    @Schema(description = "知识应用分数")
    private Integer knowledgeApplicationScore;

    @Schema(description = "问题分析")
    private String problemAnalysis;

    @Schema(description = "问题分析分数")
    private Integer problemAnalysisScore;

    @Schema(description = "工程能力")
    private String engineeringAbility;

    @Schema(description = "工程能力分数")
    private Integer engineeringAbilityScore;

    @Schema(description = "拓展能力")
    private String innovation;

    @Schema(description = "拓展能力分数")
    private Integer innovationScore;
}
