package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 16:44 2025/9/12
 */
@Data
@Schema(description = "学生学习命令")
public class StudentStudyCmd {

    @Schema(description = "课程节点ID")
    private Integer courseNodeId;
}
