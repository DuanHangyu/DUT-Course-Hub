package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 底图移动请求
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
@Schema(description = "底图移动请求")
public class HomeBackgroundMoveCmd {

    @Schema(description = "底图 ID")
    @NotNull(message = "底图 ID 不能为空")
    private Integer id;

    @Schema(description = "移动方向：up-上移，down-下移")
    @NotBlank(message = "移动方向不能为空")
    private String direction;
}
