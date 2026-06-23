package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡关指标响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "卡关指标")
public class StuckIndicatorDTO {

    @Schema(description = "卡关人数")
    private Integer stuckCount;

    @Schema(description = "高风险卡关人数")
    private Integer highRiskCount;

    @Schema(description = "中风险卡关人数")
    private Integer mediumRiskCount;

    @Schema(description = "低风险卡关人数")
    private Integer lowRiskCount;
}
