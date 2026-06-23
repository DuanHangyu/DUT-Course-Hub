package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教师编辑命令
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "教师编辑命令")
public class TeacherModifyCmd {

    @Schema(description = "教师ID（路径参数传入）")
    private Integer id;

    @Schema(description = "教师姓名")
    @NotBlank(message = "教师姓名不能为空")
    private String userName;

    @Schema(description = "登录账号")
    @NotBlank(message = "登录账号不能为空")
    private String account;

    @Schema(description = "登录密码（不传则保持原密码）")
    private String password;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    private Boolean state;
}
