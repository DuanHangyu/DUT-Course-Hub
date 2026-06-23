package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学校区域分布数据传输对象
 * 用于展示各区域的学校数量分布
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "学校区域分布信息")
public class SchoolRegionDTO {

    @Schema(description = "区域数据列表")
    private List<RegionData> regions;

    /**
     * 区域数据内部类
     * 表示单个区域的学校数量统计
     */
    @Data
    @Schema(description = "区域数据")
    public static class RegionData {

        @Schema(description = "区域/省份名称")
        private String region;

        @Schema(description = "学校数量")
        private Integer count;
    }
}
