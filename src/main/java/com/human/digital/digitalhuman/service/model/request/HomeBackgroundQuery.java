package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 底图查询请求
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
@Schema(description = "底图查询请求")
public class HomeBackgroundQuery {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer size = 10;

    @Schema(description = "图片名称")
    private String name;
}
