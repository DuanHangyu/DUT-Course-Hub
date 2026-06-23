package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务节点响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务节点")
public class TaskNodeDTO {

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "状态：0-未开始，1-进行中，2-已完成")
    private Integer status;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "考核成绩")
    private Integer score;
}
