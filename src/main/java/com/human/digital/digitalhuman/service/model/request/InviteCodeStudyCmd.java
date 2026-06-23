package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 20:31 2026/3/14
 */
@Data
@Schema(description = "邀请码学习命令")
public class InviteCodeStudyCmd {

    @Schema(description = "课程id")
    @NotNull(message = "课程id不能为空")
    private Integer courseId;

    @Schema(description = "邀请码")
    @NotEmpty(message = "邀请码不能为空")
    private String inviteCode;
}
