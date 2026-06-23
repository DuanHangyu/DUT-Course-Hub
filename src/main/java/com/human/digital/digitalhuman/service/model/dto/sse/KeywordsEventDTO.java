package com.human.digital.digitalhuman.service.model.dto.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 16:32 2025/6/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KeywordsEventDTO extends SseEventDTO{

    private List<String> content = new ArrayList<>();
}
