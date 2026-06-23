package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 风险预警项响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "风险预警项")
public class RiskWarningItemDTO {

    @Schema(description = "预警ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生学号")
    private String studentNo;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "学生头像")
    private String avatar;

    @Schema(description = "风险等级：high/medium/low")
    private String riskLevel;

    @Schema(description = "风险类型：stuck/lagging/inactive")
    private String riskType;

    @Schema(description = "风险原因")
    private String riskReason;

    @Schema(description = "关联节点ID")
    private Long nodeId;

    @Schema(description = "关联节点名称")
    private String nodeName;

    @Schema(description = "状态：0-待处理，1-已处理")
    private Integer status;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人名称")
    private String handlerName;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
