package com.human.digital.digitalhuman.service.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 22:38 2025/6/11
 */
@Data
@Schema(description = "课程节移动请求")
public class CourseNodeMoveCmd {

    @Schema(description = "课程节点id")
    @NotNull(message = "课程节id不能为空")
    @JsonProperty("courseNodeId")
    private Integer courseNodeId;

    @Schema(description = "x轴坐标")
    @JsonProperty("xAxis")
    private String xAxis;

    @Schema(description = "y轴坐标")
    @JsonProperty("yAxis")
    private String yAxis;
}
