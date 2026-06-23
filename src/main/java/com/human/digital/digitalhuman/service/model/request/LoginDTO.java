package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 15:27 2025/6/9
 */
@Data
@Schema(description = "登录参数", name = "LoginDTO")
public class LoginDTO {

    @Schema(description = "账号", name = "account")
    @NotBlank(message = "账号不能为空")
    @Size(max = 64, message = "账号长度不能超过64")
    private String account;

    @Schema(description = "密码", name = "password")
    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码长度不能超过128")
    private String password;

    @Schema(description = "是否是学生", name = "student")
    private Boolean student;

    @Schema(description = "学校ID", name = "schoolId")
    private Integer schoolId;
}
