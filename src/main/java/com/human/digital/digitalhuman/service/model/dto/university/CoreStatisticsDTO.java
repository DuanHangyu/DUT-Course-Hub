package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 核心统计DTO
 * 用于大学端数据概览的核心统计接口
 *
 * @Author taoHouChao
 * @Date 11:30 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "核心统计数据")
public class CoreStatisticsDTO {

    @Schema(description = "覆盖学校数")
    private Long schoolCount;

    @Schema(description = "累计服务学生数")
    private Long studentCount;

    @Schema(description = "累计精品课程数")
    private Long courseCount;

    @Schema(description = "AI算力消耗总量")
    private Long aiPowerCount;
}
