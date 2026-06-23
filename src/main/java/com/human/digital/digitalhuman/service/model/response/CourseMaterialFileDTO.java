package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程资料文件 DTO
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程资料文件")
public class CourseMaterialFileDTO {

    @Schema(description = "文件ID")
    private Integer id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件签名下载URL")
    private String ossUrl;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
