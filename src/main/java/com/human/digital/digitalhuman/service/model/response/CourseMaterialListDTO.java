package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程资料列表响应
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程资料列表")
public class CourseMaterialListDTO {

    @Schema(description = "文件夹列表")
    private List<CourseMaterialFolderDTO> folders;

    @Schema(description = "根目录文件列表")
    private List<CourseMaterialFileDTO> files;
}
