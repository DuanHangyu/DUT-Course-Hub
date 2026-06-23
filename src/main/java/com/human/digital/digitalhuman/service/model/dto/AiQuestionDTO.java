package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AiQuestionDTO(@Schema(description = "题目内容") String content) {
}
