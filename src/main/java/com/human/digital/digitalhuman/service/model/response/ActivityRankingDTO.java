package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活跃度排行响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活跃度排行")
public class ActivityRankingDTO {

    @Schema(description = "排名")
    private Integer rank;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "活跃度得分")
    private Integer score;
}
