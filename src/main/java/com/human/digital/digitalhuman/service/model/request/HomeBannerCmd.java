package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.HomeBannerPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Banner 请求命令
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
@Schema(description = "Banner 请求")
public class HomeBannerCmd {

    @Schema(description = "ID（修改时使用）")
    private Integer id;

    @Schema(description = "图片名称")
    @NotBlank(message = "图片名称不能为空")
    private String name;

    @Schema(description = "图片URL")
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    public HomeBannerPO toPo() {
        HomeBannerPO po = new HomeBannerPO();
        po.setName(name);
        po.setImageUrl(imageUrl);
        return po;
    }
}
