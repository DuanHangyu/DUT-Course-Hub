package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 22:22 2025/7/10
 */
@Data
@Schema(description = "学习记录修改请求")
public class LearningRecordModifyDTO {

    @Schema(description = "学习记录ID")
    private Integer id;

    @Schema(description = "教师核分")
    private Integer teacherCheckScore;
}
