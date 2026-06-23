package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重命名课程资料文件请求
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Schema(description = "重命名课程资料文件请求")
public class CourseMaterialFileRenameCmd {

    @Schema(description = "文件ID")
    @NotNull(message = "文件ID不能为空")
    private Integer id;

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @Schema(description = "新文件名")
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String fileName;
}
