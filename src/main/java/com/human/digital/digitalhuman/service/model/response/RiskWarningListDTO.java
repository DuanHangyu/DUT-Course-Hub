package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险预警列表响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "风险预警列表")
public class RiskWarningListDTO {

    @Schema(description = "预警列表")
    private List<RiskWarningItemDTO> warnings;

    @Schema(description = "高风险人数")
    private Integer highRiskCount;

    @Schema(description = "中风险人数")
    private Integer mediumRiskCount;

    @Schema(description = "低风险人数")
    private Integer lowRiskCount;

    @Schema(description = "总人数")
    private Integer totalCount;
}
