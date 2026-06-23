package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 11:20 2025/6/15
 */
@Data
@Schema(description = "学习记录列表查询参数")
public class AssessmentResultListQuery {

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程节点名称")
    private String courseNodeName;

    @Schema(description = "考核模式 1节点考核 2终极考核")
    private Integer assessMode;

    @Schema(description = "当前页码")
    @NotNull(message = "当前页码不能为空")
    private Integer page;

    @Schema(description = "每页数量")
    @NotNull(message = "每页数量不能为空")
    private Integer size;
}
