package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 22:27 2026/3/9
 */
@Data
@Schema(description = "添加学生关系")
public class AddRelationStudentCmd {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "学生ID列表")
    private List<Integer> studentIds;

    @Schema(description = "班级ID列表")
    private List<Integer> classIds;
}
