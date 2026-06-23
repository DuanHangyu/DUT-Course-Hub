package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 进度指标响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "进度指标")
public class ProgressIndicatorDTO {

    @Schema(description = "当前进度百分比")
    private BigDecimal currentProgress;

    @Schema(description = "周环比变化（正数为增长，负数为下降）")
    private BigDecimal weekOverWeek;

    @Schema(description = "学生总数")
    private Integer totalStudents;

    @Schema(description = "已完成学生数")
    private Integer completedStudents;
}
