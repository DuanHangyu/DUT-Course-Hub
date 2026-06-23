package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活跃度指标响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活跃度指标")
public class ActivityIndicatorDTO {

    @Schema(description = "活跃度指数（0-100）")
    private Integer activityIndex;

    @Schema(description = "周环比变化")
    private Integer weekOverWeek;

    @Schema(description = "提问总数")
    private Integer questionCount;

    @Schema(description = "评论总数")
    private Integer commentCount;

    @Schema(description = "活跃学生数")
    private Integer activeStudents;
}
