package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学习趋势数据传输对象
 * 用于展示各学校每日学习人数趋势数据
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "学习趋势信息")
public class LearningTrendDTO {

    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "趋势线列表")
    private List<TrendLine> lines;

    /**
     * 趋势线内部类
     * 表示单个学校的学习趋势数据
     */
    @Data
    @Schema(description = "趋势线")
    public static class TrendLine {

        @Schema(description = "学校名称")
        private String schoolName;

        @Schema(description = "每日学习人数列表")
        private List<Integer> values;
    }
}
