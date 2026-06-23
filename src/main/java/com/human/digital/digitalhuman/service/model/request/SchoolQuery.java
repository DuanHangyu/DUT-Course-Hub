package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学校查询对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Data
@Schema(description = "学校查询")
public class SchoolQuery {

    @Schema(description = "学校名称（模糊查询）")
    private String schoolName;

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer size = 10;

    public Integer getPage() {
        return page == null ? 1 : page;
    }

    public Integer getSize() {
        return size == null ? 10 : size;
    }
}
