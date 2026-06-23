package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交记录DTO
 *
 * @Author taoHouChao
 * @Date 10:15 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "作业提交记录")
public class AssignmentRecordDTO {

    @Schema(description = "作业ID")
    private Long assignmentId;

    @Schema(description = "作业标题")
    private String title;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "状态（0-未提交，1-已提交，2-已批改）")
    private Integer status;

    @Schema(description = "成绩")
    private BigDecimal score;
}
