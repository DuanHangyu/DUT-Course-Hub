package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.service.model.dto.QuestionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 16:02 2025/6/16
 */
@Data
@Schema(description = "保存答案请求")
public class AnswerSaveCmd {

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "课程节点ID")
    private Integer courseNodeId;

    @Schema(description = "问题")
    @NotEmpty(message = "问题不能为空")
    private QuestionDTO question;

    @Schema(description = "答案")
    @NotEmpty(message = "答案不能为空")
    private String answer;

    public boolean finalAssess(){
        return courseId != null && courseNodeId == null;
    }

    public boolean nodeAssess(){
        return courseId != null && courseNodeId != null;
    }
}
