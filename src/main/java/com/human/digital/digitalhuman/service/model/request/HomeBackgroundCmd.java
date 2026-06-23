package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.HomeBackgroundPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 底图请求命令
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
@Schema(description = "底图请求")
public class HomeBackgroundCmd {

    @Schema(description = "ID（修改时使用）")
    private Integer id;

    @Schema(description = "图片名称")
    @NotBlank(message = "图片名称不能为空")
    private String name;

    @Schema(description = "图片URL")
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    @Schema(description = "跳转链接")
    private String jumpUrl;

    public HomeBackgroundPO toPo() {
        HomeBackgroundPO po = new HomeBackgroundPO();
        po.setName(name);
        po.setImageUrl(imageUrl);
        po.setJumpUrl(jumpUrl);
        return po;
    }
}
