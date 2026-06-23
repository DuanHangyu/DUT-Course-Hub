package com.human.digital.digitalhuman.service.model.dto.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 16:31 2025/6/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubqueryEventDTO extends SseEventDTO{

    private List<String> content = new ArrayList<>();
}
