package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @USER taoHouChao
 * @DATE 16:36 2026/3/5
 */
@Data
@Schema(description = "学校简易信息")
@AllArgsConstructor
@NoArgsConstructor
public class SchoolSimpleDTO {

    @Schema(description = "学校ID")
    private Integer id;

    @Schema(description = "学校名称")
    private String schoolName;
}
