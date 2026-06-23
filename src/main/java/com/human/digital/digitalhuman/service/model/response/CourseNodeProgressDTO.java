package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 课程节点进度DTO
 *
 * @Author taoHouChao
 * @Date 10:10 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程节点进度")
public class CourseNodeProgressDTO {

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点颜色")
    private String nodeColour;

    @Schema(description = "学习时长（分钟）")
    private Integer duration;

    @Schema(description = "完成进度")
    private BigDecimal progress;

    @Schema(description = "状态（0-未开始，1-进行中，2-已完成）")
    private Integer status;
}
