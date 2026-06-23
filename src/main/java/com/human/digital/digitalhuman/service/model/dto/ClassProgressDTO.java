package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 班级学习进度DTO
 *
 * @Author taoHouChao
 * @Date 10:20 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "班级学习进度")
public class ClassProgressDTO {

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "完成率（百分比）")
    private BigDecimal completionRate;

    public Double completionRateDouble() {
        return completionRate.doubleValue();
    }
}
