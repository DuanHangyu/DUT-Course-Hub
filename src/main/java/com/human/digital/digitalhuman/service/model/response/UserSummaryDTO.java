package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author taoHouChao
 * @Date 14:47 2025/6/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户摘要信息")
public class UserSummaryDTO {

    @Schema(description = "用户id")
    private Integer id;

    @Schema(description = "用户账户")
    private String account;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户状态 true正常， false已下架")
    private Boolean state;

    @Schema(description = "用户创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "用户更新时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
