package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 系统健康状态数据传输对象
 * 用于展示系统各指标的健康状态
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "系统健康状态信息")
public class SystemHealthDTO {

    @Schema(description = "总体状态")
    private String status;

    @Schema(description = "健康项列表")
    private List<HealthItem> items;

    /**
     * 健康项内部类
     * 表示单个系统指标的的健康状态
     */
    @Data
    @Schema(description = "健康项")
    public static class HealthItem {

        @Schema(description = "指标名称")
        private String name;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "描述信息")
        private String message;
    }
}
