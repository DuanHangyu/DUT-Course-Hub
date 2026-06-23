package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 10:46 2025/6/12
 */
@Data
public class UserModifyCmd {

    @Schema(description = "用户id")
    @NotNull(message = "用户id不能为空")
    private Integer id;

    @Schema(description = "账户")
    private String account;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "状态")
    private Boolean state;
}
