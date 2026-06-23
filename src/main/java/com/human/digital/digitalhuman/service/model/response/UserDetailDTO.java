package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author taoHouChao
 * @Date 10:53 2025/6/12
 */
@Data
@Schema(description = "用户详情")
public class UserDetailDTO {

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

    @Schema(description = "角色 0超级管理员 1学校管理员 2老师")
    private Integer role;

    @Schema(description = "学校信息")
    private SchoolSimpleDTO school;

    @Schema(description = "功能模块授权")
    private List<String> authModules;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime  createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime  updateTime;
}
