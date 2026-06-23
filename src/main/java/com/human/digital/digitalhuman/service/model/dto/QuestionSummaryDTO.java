package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 17:37 2025/6/28
 */
@Data
public class QuestionSummaryDTO {

    @Schema(description = "问题id")
    private Long id;

    @Schema(description = "论文总数")
    private Integer paperTotal;

    @Schema(description = "论文")
    private List<PapersDTO> papers;

    @Schema(description = "总结")
    private String summary;

    @Schema(description = "相关问题")
    private List<String> related;
}
