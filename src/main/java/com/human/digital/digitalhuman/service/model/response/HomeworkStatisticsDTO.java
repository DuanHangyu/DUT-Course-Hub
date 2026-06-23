package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 作业统计响应对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业统计")
public class HomeworkStatisticsDTO {

    @Schema(description = "作业ID")
    private Long homeworkId;

    @Schema(description = "作业名称")
    private String homeworkName;

    @Schema(description = "提交率(%)")
    private BigDecimal submitRate;

    @Schema(description = "已提交人数")
    private Integer submittedCount;

    @Schema(description = "未提交人数")
    private Integer unsubmittedCount;
}
