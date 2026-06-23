package com.human.digital.digitalhuman.service.model.dto.sse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @USER taoHouChao
 * @DATE 16:32 2025/6/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeBaseTotalEventDTO extends SseEventDTO{

    @JsonProperty("content")
    private KnowledgeBaseTotal content;

    @Data
    public static class KnowledgeBaseTotal{

        private Integer total;
    }
}
