package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 15:04 2025/6/16
 */
@Data
@Schema(description = "生成问题参数")
public class QuestionGenerateCmd {

    @Schema(description = "课程节点ID")
    @NotNull(message = "课程节点ID不能为空")
    private Integer courseNodeId;
}
