package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学习活力热力图响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学习活力热力图")
public class ActivityHeatmapDTO {

    @Schema(description = "日期范围(近7天,格式MM-dd)")
    private List<String> dateRange;

    @Schema(description = "学生热力图数据列表")
    private List<StudentHeatmapData> students;

    /**
     * 学生热力图数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "学生热力图数据")
    public static class StudentHeatmapData {

        @Schema(description = "学生ID")
        private Long studentId;

        @Schema(description = "学生姓名")
        private String studentName;

        @Schema(description = "每日活力值(与dateRange一一对应,范围0-100)")
        private List<Integer> dailyScores;
    }
}
