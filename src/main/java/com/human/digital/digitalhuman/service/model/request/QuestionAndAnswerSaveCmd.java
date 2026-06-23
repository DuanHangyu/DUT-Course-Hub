package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 09:34 2025/6/19
 */
@Data
@Schema(description = "保存问题答案请求")
public class QuestionAndAnswerSaveCmd {

    @Schema(description = "问题")
    private String question;

    @Schema(description = "答案")
    private String answer;
}
