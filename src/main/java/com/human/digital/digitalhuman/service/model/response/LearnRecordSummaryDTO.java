package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 11:12 2025/9/12
 */
@Data
@Schema(description = "学习记录摘要信息")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LearnRecordSummaryDTO {

    @Schema(description = "学生ID")
    private Integer studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "班级")
    private String schoolClass;

    @Schema(description = "参与课程数")
    private Integer participateCourseCount;

    @Schema(description = "参与课程信息")
    private List<CourseDTO> participateCourseList;

    @Schema(description = "已完成课程数")
    private Integer alreadyCompleteCourseCount;

    @Schema(description = "已完成课程信息")
    private List<CourseDTO> alreadyCompleteCourseList;

    @Schema(description = "待学习课程数")
    private Integer waitStudyCourseCount;

    @Schema(description = "待学习课程信息")
    private List<CourseDTO> waitStudyCourseList;

    @Schema(description = "最近学习时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime lastStudyTime;

    @Data
    @Schema(description = "课程信息")
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CourseDTO{

        @Schema(description = "课程名称")
        private String courseName;

        @Schema(description = "最新考核时间")
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
        private LocalDateTime lastAssessmentTime;
    }
}
