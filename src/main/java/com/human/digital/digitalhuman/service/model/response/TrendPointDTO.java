package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 趋势数据点响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据点")
public class TrendPointDTO {

    @Schema(description = "日期（yyyy-MM-dd）")
    private String date;

    @Schema(description = "平均进度(%)")
    private BigDecimal avgProgress;
}
