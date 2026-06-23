package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @USER taoHouChao
 * @DATE 22:11 2026/3/5
 */
@Data
@Schema(description = "课程模板状态统计")
@AllArgsConstructor
@NoArgsConstructor
public class CourseTemplateStatusCountDTO {

    @Schema(description = "课程模板上线数量")
    private Long onlineCount;

    @Schema(description = "课程模板下线数量")
    private Long offlineCount;
}
