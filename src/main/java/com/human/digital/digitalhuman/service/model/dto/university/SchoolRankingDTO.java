package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学校排名数据传输对象
 * 用于展示学校排名数据，包含排名、学校名称和活跃度信息
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "学校排名信息")
public class SchoolRankingDTO {

    @Schema(description = "排名")
    private Integer rank;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "活跃度")
    private Long activity;
}
