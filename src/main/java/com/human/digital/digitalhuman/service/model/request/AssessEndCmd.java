package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @USER taoHouChao
 * @DATE 21:28 2025/6/16
 */
@Data
@Schema(description = "结束考核参数")
public class AssessEndCmd {

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "课程节点ID")
    private Integer courseNodeId;

    @Schema(description = "是否是终极考核，true终极考核，false节点考核")
    @NotNull(message = "是否是终极考核不能为空")
    private Boolean lastNode;

    @Schema(description = "是否是无问题结束")
    private Boolean noQuestionEnd;
}
