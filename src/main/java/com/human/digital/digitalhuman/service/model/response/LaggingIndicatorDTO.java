package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 滞后指标响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "滞后指标")
public class LaggingIndicatorDTO {

    @Schema(description = "滞后人数")
    private Integer laggingCount;

    @Schema(description = "高风险滞后人数")
    private Integer highRiskCount;

    @Schema(description = "中风险滞后人数")
    private Integer mediumRiskCount;

    @Schema(description = "低风险滞后人数")
    private Integer lowRiskCount;
}
