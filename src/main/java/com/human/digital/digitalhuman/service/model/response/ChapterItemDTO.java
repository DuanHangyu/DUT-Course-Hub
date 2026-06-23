package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 章节项响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "章节项")
public class ChapterItemDTO {

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "章节名称")
    private String chapterName;

    @Schema(description = "完成率(%)")
    private Double completionRate;

    @Schema(description = "状态：0-未开始，1-进行中，2-已完成")
    private Integer status;
}
