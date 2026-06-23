package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 24小时流量数据传输对象
 * 用于展示24小时内每小时的访问量数据
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "24小时流量信息")
public class Traffic24hDTO {

    @Schema(description = "小时列表")
    private List<String> hours;

    @Schema(description = "每小时访问量列表")
    private List<Integer> values;
}
