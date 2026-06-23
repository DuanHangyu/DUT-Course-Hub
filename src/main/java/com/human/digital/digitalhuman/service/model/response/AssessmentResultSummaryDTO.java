package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * @USER taoHouChao
 * @DATE 11:15 2025/6/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "考核结果摘要")
public class AssessmentResultSummaryDTO {

    @Schema(description = "学习记录ID")
    private Integer id;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程节点名称")
    private String courseNodeName;

    @Schema(description = "考核模式 1节点考核 2终极考核")
    private Integer assessMode;

    @Schema(description = "考核分数")
    private Integer assessScore;

    @Schema(description = "教师核分")
    private Integer teacherCheckScore;

    @Schema(description = "学习时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime studyTime;

    public static AssessmentResultSummaryDTO fromPO(StudentCourseAssessPO po,
                                                    Function<Integer, String> studentNameConverter,
                                                    Function<Integer, String> courseNameConverter,
                                                    Function<Integer, String> courseNodeNameConverter) {
        return AssessmentResultSummaryDTO.builder()
                .id(po.getId())
                .studentName(studentNameConverter.apply(po.getStudentId()))
                .courseName(courseNameConverter.apply(po.getCourseId()))
                .courseNodeName(courseNodeNameConverter.apply(po.getCourseNodeId()))
                .assessMode(po.getAssessMode())
                .assessScore(po.getAssessScore())
                .teacherCheckScore(po.getTeacherCheckScore())
                .studyTime(po.getCreateTime())
                .build();
    }
}
