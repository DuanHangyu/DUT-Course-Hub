package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 核心指标概览响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "核心指标概览")
public class CoreIndicatorOverviewDTO {

    @Schema(description = "平均进度")
    private ProgressIndicatorDTO progress;

    @Schema(description = "思维活跃度")
    private ActivityIndicatorDTO activity;

    @Schema(description = "卡关预警")
    private StuckIndicatorDTO stuck;

    @Schema(description = "进度滞后")
    private LaggingIndicatorDTO lagging;
}
