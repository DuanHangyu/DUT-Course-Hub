package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 节点图数据响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点图数据")
public class NodeGraphDTO {

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "关联节点")
    private List<Integer> relateNode;

    @Schema(description = "节点颜色")
    private String nodeColour;

    @Schema(description = "节点大小")
    private Integer nodeSize;

    @Schema(description = "完成率")
    private BigDecimal completionRate;

    @Schema(description = "X坐标")
    private String xAxis;

    @Schema(description = "Y坐标")
    private String yAxis;
}
