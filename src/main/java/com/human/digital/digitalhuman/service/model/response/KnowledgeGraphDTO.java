package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识图谱响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识图谱")
public class KnowledgeGraphDTO {

    @Schema(description = "节点列表")
    private List<NodeGraphDTO> nodes;

    @Schema(description = "连接列表")
    private List<KnowledgeLinkDTO> links;
}
