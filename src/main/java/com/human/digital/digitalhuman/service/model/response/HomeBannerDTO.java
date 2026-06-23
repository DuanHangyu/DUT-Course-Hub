package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.human.digital.digitalhuman.repository.po.HomeBannerPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * Banner 响应
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
public class HomeBannerDTO {

    private Integer id;

    @Schema(description = "图片名称")
    private String name;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "跳转链接")
    private String url;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    public static HomeBannerDTO of(HomeBannerPO po) {
        return of(po, null);
    }

    public static HomeBannerDTO of(HomeBannerPO po, Function<String, String> signedUrlFunc) {
        HomeBannerDTO dto = new HomeBannerDTO();
        dto.setId(po.getId());
        dto.setName(po.getName());
        dto.setImageUrl(po.getImageUrl());
        dto.setUrl(po.getImageUrl());
        dto.setSort(po.getSort());
        dto.setCreateTime(po.getCreateTime());
        // 生成 OSS 签名 URL
        if (signedUrlFunc != null && dto.getImageUrl() != null) {
            dto.setImageUrl(signedUrlFunc.apply(dto.getImageUrl()));
        }
        return dto;
    }
}
