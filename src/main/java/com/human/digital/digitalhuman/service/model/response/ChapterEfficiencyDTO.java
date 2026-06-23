package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 章节效率响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "章节效率分布")
public class ChapterEfficiencyDTO {

    @Schema(description = "课程平均完成度 70.1", example = "70.1")
    private BigDecimal courseCompletion;

    @Schema(description = "最快进度", example = "95.5")
    private BigDecimal fastestProgress;

    @Schema(description = "最快进度人数", example = "2")
    private Integer fastestProgressCount;

    @Schema(description = "最慢进度", example = "65.5")
    private BigDecimal slowestProgress;

    @Schema(description = "最慢进度人数", example = "1")
    private Integer slowestProgressCount;

    @Schema(description = "进度列表", example = "[0%, 1-20%, 21-40%, 41-60%, 61-80%, 81-99%, 100%]")
    private List<String> progressList;

    @Schema(description = "进度人数列表", example = "[0, 1, 2, 3, 4, 5, 6]")
    private List<Integer> progressCountList;
}
