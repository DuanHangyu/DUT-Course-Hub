package com.human.digital.digitalhuman.service.model.dto.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @USER taoHouChao
 * @DATE 16:34 2025/6/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SummaryEventDTO extends SseEventDTO{

    private String content;
}
