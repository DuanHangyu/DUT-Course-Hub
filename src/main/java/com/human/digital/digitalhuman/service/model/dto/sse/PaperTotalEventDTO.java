package com.human.digital.digitalhuman.service.model.dto.sse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @USER taoHouChao
 * @DATE 16:33 2025/6/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PaperTotalEventDTO extends SseEventDTO{

    @JsonProperty("content")
    private PaperTotal content;

    @Data
    public static class PaperTotal{

        private Integer total;
    }
}
