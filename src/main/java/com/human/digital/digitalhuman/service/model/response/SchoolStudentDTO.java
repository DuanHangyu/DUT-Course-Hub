package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 12:27 2025/9/15
 */
@Data
public class SchoolStudentDTO {

    @Schema(description = "班级id")
    private Integer id;

    @Schema(description = "学校名称")
    private String schoolClass;

    @Schema(description = "学生列表")
    private List<StudentSummaryDTO> students;
}
