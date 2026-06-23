package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 09:36 2025/9/18
 */
@Data
@Schema(description = "开始最终考试请求")
public class StartFinalAssessCmd {

    @Schema(description = "课程ID")
    private Integer courseId;
}
