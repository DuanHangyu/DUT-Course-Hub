package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author taoHouChao
 * @Date 22:34 2025/6/10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "学生摘要信息")
public class StudentSummaryDTO {

    @Schema(description = "学生id")
    private Integer id;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "班级")
    private String schoolClass;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "学生创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "学生更新时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
