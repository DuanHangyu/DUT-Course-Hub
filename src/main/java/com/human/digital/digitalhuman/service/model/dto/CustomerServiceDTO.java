package com.human.digital.digitalhuman.service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服对话请求/响应 DTO
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@Data
public class CustomerServiceDTO {

    @Schema(description = "用户ID")
    @JsonIgnore
    private Integer userId;

    @Schema(description = "用户问题")
    private String message;

    @Schema(description = "会话ID（可选，用于多轮对话）")
    private String sessionId;

    @Schema(description = "答案")
    @JsonIgnore
    private String answer;

    @Schema(description = "意图类型")
    @JsonIgnore
    private String intentType;
}
