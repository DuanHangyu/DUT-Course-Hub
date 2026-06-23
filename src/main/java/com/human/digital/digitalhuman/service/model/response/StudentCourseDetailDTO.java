package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * @USER taoHouChao
 * @DATE 14:11 2025/9/12
 */
@Data
@Schema(description = "学生课程详情")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentCourseDetailDTO {

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "表格视图")
    private TableViewDTO tableView;

    @Schema(description = "节点视图")
    private CourseDetailDTO nodeView;

    @Data
    @Schema(description = "表格视图")
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TableViewDTO{

        @Schema(description = "学习节点")
        private List<StudyNodeDTO> studyNodeList;
    }

    @Data
    @Schema(description = "学习节点")
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StudyNodeDTO{

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "学习状态, 0未学习，1学习中，2已完成")
        private Integer state;

        @Schema(description = "学习时间")
        private Integer studyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StudentCourseDetailDTO that = (StudentCourseDetailDTO) o;
        return Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(courseId);
    }
}
