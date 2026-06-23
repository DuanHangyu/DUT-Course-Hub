package com.human.digital.digitalhuman.service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @USER taoHouChao
 * @DATE 21:58 2025/6/16
 */
@Data
@Schema(description = "学生信息")
public class StudentDTO {

    @Schema(description = "学生ID")
    private Integer id;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
