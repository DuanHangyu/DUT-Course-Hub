package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 上传课程资料文件请求
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Schema(description = "上传课程资料文件请求")
public class CourseMaterialFileUploadCmd {

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @Schema(description = "文件夹ID，不传表示根目录")
    private Integer folderId;

    @Schema(description = "文件名")
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String fileName;

    @Schema(description = "OSS存储路径")
    @NotBlank(message = "OSS存储路径不能为空")
    @Size(max = 500, message = "OSS存储路径不能超过500个字符")
    private String ossKey;

    @Schema(description = "文件大小(字节)")
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    @Schema(description = "文件类型/后缀")
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 50, message = "文件类型不能超过50个字符")
    private String fileType;
}
