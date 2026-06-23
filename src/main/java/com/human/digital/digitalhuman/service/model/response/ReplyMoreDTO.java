package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 01:08 2025/9/13
 */
@Data
@Schema(description = "课程学生回复")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyMoreDTO {

    @Schema(description = "是否有更多")
    private Boolean hasMore;

    @Schema(description = "回复")
    private List<ReplyDTO> replies;
}
