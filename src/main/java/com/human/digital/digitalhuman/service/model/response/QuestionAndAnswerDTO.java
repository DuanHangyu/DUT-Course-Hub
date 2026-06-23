package com.human.digital.digitalhuman.service.model.response;

import com.human.digital.digitalhuman.service.model.dto.QuestionSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @USER taoHouChao
 * @DATE 09:54 2025/6/19
 */
@Data
@Schema(description = "问题答案")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionAndAnswerDTO {

    @Schema(description = "问题")
    private String question;

    @Schema(description = "答案")
    private String answer;

    @Schema(description = "论文")
    private QuestionSummaryDTO papers;
}
