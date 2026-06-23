package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 21:54 2026/3/7
 */
@Data
@Schema(description = "教师状态切换命令")
public class TeacherStateChangeCmd {

    @Schema(description = "教师ID")
    @NotNull(message = "教师ID不能为空")
    private Integer id;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    private Boolean state;
}
