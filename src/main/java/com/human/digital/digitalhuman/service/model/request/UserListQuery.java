package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 14:50 2025/6/11
 */
@Data
@Schema(description = "用户列表查询参数")
public class UserListQuery {

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户账号")
    private String account;

    @Schema(description = "用户状态")
    private Boolean state;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
