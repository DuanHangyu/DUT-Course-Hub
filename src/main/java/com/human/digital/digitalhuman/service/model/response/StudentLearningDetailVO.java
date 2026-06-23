package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生学习详情响应
 *
 * @Author taoHouChao
 * @Date 10:20 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生学习详情")
public class StudentLearningDetailVO {

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "课程综合进度")
    private BigDecimal courseProgress;

    @Schema(description = "作业提交率")
    private BigDecimal assignmentSubmitRate;

    @Schema(description = "最近活跃时间")
    private LocalDateTime lastActiveTime;

    @Schema(description = "节点进度列表")
    private List<CourseNodeProgressDTO> nodeProgressList;

    @Schema(description = "作业记录列表")
    private List<AssignmentRecordDTO> assignmentRecords;
}
