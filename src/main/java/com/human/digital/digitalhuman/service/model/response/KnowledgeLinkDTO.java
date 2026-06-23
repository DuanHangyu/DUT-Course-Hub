package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识图谱连接响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识图谱连接")
public class KnowledgeLinkDTO {

    @Schema(description = "源节点ID")
    private Long source;

    @Schema(description = "目标节点ID")
    private Long target;
}
