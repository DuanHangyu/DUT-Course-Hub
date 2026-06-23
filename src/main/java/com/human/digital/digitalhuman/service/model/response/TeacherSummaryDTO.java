package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师摘要信息
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "教师摘要信息")
public class TeacherSummaryDTO {

    @Schema(description = "教师ID")
    private Integer id;

    @Schema(description = "教师姓名")
    private String userName;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用，1-启用")
    private Boolean state;

    @Schema(description = "所属学校ID")
    private Integer schoolId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
