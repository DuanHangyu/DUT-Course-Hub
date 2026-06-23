package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 18:53 2025/6/19
 */
@Data
@Schema(description = "问题答案记录")
public class QuestionAnswerLogDTO {

    @Schema(description = "时间名称", example = "今天")
    private String name;

    @Schema(description = "问题答案记录")
    private List<QuestionAndAnswerDTO> records;
}
