package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 17:37 2025/6/28
 */
@Data
public class PapersDTO {

    @Schema(description = "作者")
    private List<String> author;

    @Schema(description = "中文标题")
    private String titleZh;

    @Schema(description = "中文摘要")
    private String abstractZh;

    @Schema(description = "发布时间")
    private String publicationDate;

    @Schema(description = "期刊")
    private String journal;
}
