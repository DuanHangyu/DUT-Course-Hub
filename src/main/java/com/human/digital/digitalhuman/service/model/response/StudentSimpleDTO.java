package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生简单信息响应对象
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生简单信息")
public class StudentSimpleDTO {

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "学生头像")
    private String avatar;
}
