package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学习时间热力图DTO
 *
 * @Author taoHouChao
 * @Date 10:15 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学习时间热力图")
public class HeatmapDTO {

    @Schema(description = "学生姓名列表")
    private List<String> students;

    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "热力值矩阵")
    private List<List<Integer>> values;
}
