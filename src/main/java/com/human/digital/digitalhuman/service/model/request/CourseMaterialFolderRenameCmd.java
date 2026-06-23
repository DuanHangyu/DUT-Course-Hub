package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重命名课程资料文件夹请求
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Schema(description = "重命名课程资料文件夹请求")
public class CourseMaterialFolderRenameCmd {

    @Schema(description = "文件夹ID")
    @NotNull(message = "文件夹ID不能为空")
    private Integer id;

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @Schema(description = "新文件夹名称")
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 100, message = "文件夹名称不能超过100个字符")
    private String folderName;
}
