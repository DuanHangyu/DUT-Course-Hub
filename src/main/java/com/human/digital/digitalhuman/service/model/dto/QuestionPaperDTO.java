package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 22:59 2025/6/28
 */
@Data
public class QuestionPaperDTO {

    @Schema(description = "论文")
    private List<PapersDTO> list;
}
