package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 13:55 2025/9/13
 */
@Data
@Schema(description = "课程学生提问返回")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {

    private Long total;

    private List<CommentDTO> comments;
}
