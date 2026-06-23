package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 19:33 2025/6/17
 */
@Data
@Schema(description = "分页查询参数")
public class PageQueryDTO {

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
