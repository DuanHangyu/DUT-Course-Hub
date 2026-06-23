package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 09:32 2025/9/18
 */
@Data
@Schema(description = "开始考核请求")
public class StartNodeAssessCmd {

    @Schema(description = "课程节点ID")
    private Integer courseNodeId;
}
