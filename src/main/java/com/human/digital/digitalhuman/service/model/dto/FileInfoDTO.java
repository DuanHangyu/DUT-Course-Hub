package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 20:07 2025/6/14
 */
@Data
@Schema(description = "文件信息")
public class FileInfoDTO {

    @Schema(description = "文件地址")
    private String url;

    @Schema(description = "文件名称")
    private String fileName;
}
