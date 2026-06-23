package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 概览摘要DTO
 * 包含4个核心指标卡片数据
 *
 * @Author taoHouChao
 * @Date 10:05 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "概览摘要")
public class OverviewSummaryDTO {

    @Schema(description = "实时活跃人数")
    private Integer activeCount;

    @Schema(description = "作业平均提交率（百分比）")
    private BigDecimal homeworkRate;

    @Schema(description = "AI助教响应数")
    private Long aiResponseCount;

    @Schema(description = "风险预警班级数")
    private Integer riskClassCount;
}
