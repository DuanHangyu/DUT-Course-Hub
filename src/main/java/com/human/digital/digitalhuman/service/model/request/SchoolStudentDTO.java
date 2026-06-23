package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 12:34 2025/9/15
 */
@Data
@Schema(description = "学校学生信息")
public class SchoolStudentDTO {

    @Schema(description = "学校名称")
    private String schoolClass;

    @Schema(description = "学生id集合")
    private List<Integer> studentIds;
}
