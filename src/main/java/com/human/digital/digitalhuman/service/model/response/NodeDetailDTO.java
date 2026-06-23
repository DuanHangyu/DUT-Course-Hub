package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 节点详情响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点详情")
public class NodeDetailDTO {

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "完成人数")
    private Integer completedCount;

    @Schema(description = "未完成人数")
    private Integer incompleteCount;

    @Schema(description = "完成率")
    private BigDecimal completionRate;

    @Schema(description = "已完成学生列表")
    private List<StudentSimpleDTO> completedStudents;

    @Schema(description = "未完成学生列表")
    private List<StudentSimpleDTO> incompleteStudents;
}
