package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @USER taoHouChao
 * @DATE 23:31 2025/9/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件参数")
public class FileDTO {

    @Schema(description = "文件地址")
    private String url;

    @Schema(description = "文件名称")
    private String fileName;
}
