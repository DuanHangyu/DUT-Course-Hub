package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程资料文件夹 DTO
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程资料文件夹")
public class CourseMaterialFolderDTO {

    @Schema(description = "文件夹ID")
    private Integer id;

    @Schema(description = "文件夹名称")
    private String folderName;

    @Schema(description = "文件数量")
    private Long fileCount;

    @Schema(description = "排序序号")
    private Integer sortOrder;
}
