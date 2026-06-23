package com.human.digital.digitalhuman.service.model.dto.sse;

import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 16:30 2025/6/15
 */
@Data
public class SseEventDTO {

    private String id;
    private String type;
    private String finishReason;
    private String receivedAt;
    private String sendedAt;
}
