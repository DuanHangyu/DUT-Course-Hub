package com.human.digital.digitalhuman.service.model.response;

import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * 作业文件信息
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "作业文件信息")
public class HomeworkFileDTO {

    @Schema(description = "文件地址")
    private String url;

    @Schema(description = "文件签名地址")
    private String presignedUrl;

    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 从 UploadDTO 转换
     */
    public static HomeworkFileDTO from(UploadDTO uploadDTO, Function<String, String> urlTransformer) {
        if (uploadDTO == null) {
            return null;
        }
        return new HomeworkFileDTO(uploadDTO.getUrl(), urlTransformer.apply(uploadDTO.getUrl()), uploadDTO.getFileName());
    }
}
