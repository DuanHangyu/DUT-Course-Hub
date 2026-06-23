package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教师列表查询
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "教师列表查询")
public class TeacherListQuery {

    @Schema(description = "教师姓名")
    private String userName;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "学校ID（超级管理员传递）")
    private Integer schoolId;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
