package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务流程响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务流程")
public class TaskFlowDTO {

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "任务节点列表")
    private List<TaskNodeDTO> taskNodes;
}
