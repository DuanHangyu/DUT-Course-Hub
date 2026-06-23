package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 课程资料排序请求
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Schema(description = "课程资料排序请求")
public class CourseMaterialSortCmd {

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @Schema(description = "排序项列表")
    @NotEmpty(message = "排序项不能为空")
    private List<SortItem> sortItems;

    @Data
    @Schema(description = "排序项")
    public static class SortItem {
        @Schema(description = "ID")
        @NotNull(message = "ID不能为空")
        private Integer id;

        @Schema(description = "排序序号")
        @NotNull(message = "排序序号不能为空")
        private Integer sortOrder;
    }
}
