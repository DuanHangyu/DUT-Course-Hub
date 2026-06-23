package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生进度DTO
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生进度")
public class StudentProgressDTO {

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "课程综合进度")
    private BigDecimal courseProgress;

    @Schema(description = "已提交作业数")
    private Integer submittedCount;

    @Schema(description = "作业总数")
    private Integer totalAssignmentCount;

    @Schema(description = "当前学习章节名称")
    private String currentNodeName;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "是否预警状态")
    private Boolean isWarning;
}
