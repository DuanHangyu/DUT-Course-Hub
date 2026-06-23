package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班级查询对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "班级查询")
public class ClassQuery {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer size = 10;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "学校ID")
    private Integer schoolId;
}
