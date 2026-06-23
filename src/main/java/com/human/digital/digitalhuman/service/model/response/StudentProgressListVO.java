package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学生进度列表响应
 *
 * @Author taoHouChao
 * @Date 10:05 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生进度列表响应")
public class StudentProgressListVO {

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "预警学生数")
    private Integer warningCount;

    @Schema(description = "学生列表")
    private List<StudentProgressDTO> list;
}
