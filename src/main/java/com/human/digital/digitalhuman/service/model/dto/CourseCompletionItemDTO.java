package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 课程完成度项DTO
 * 用于表示单个课程的完成度信息
 *
 * @Author taoHouChao
 * @Date 14:20 2026/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程完成度项")
public class CourseCompletionItemDTO {

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "完成率（百分比）")
    private BigDecimal completionRate;

    @Schema(description = "已完成人数")
    private Integer completedCount;

    @Schema(description = "未完成人数")
    private Integer uncompletedCount;

    @Schema(description = "总学生数")
    private Integer totalCount;
}
