package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 风险预警处理请求
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@Schema(description = "风险预警处理请求")
public class RiskWarningHandleCmd {

    @NotNull(message = "预警ID不能为空")
    @Schema(description = "预警ID")
    private Long id;
}
