package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 章节进度响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "章节进度")
public class ChapterProgressDTO {

    @Schema(description = "总章节数")
    private Integer totalChapters;

    @Schema(description = "已完成章节数")
    private Integer completedChapters;

    @Schema(description = "章节列表")
    private List<ChapterItemDTO> chapters;
}
