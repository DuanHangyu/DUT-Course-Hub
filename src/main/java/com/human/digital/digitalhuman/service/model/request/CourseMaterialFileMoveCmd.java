package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动课程资料文件请求
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Schema(description = "移动课程资料文件请求")
public class CourseMaterialFileMoveCmd {

    @Schema(description = "文件ID")
    @NotNull(message = "文件ID不能为空")
    private Integer id;

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @Schema(description = "目标文件夹ID，null表示移到根目录")
    private Integer folderId;
}
