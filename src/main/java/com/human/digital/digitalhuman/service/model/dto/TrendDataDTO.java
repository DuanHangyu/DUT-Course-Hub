package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势数据DTO
 * 用于折线图等趋势展示
 *
 * @Author taoHouChao
 * @Date 10:10 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据")
public class TrendDataDTO {

    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "数值列表")
    private List<Integer> values;
}
