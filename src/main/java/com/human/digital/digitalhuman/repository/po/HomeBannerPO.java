package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页 Banner 实体类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
@TableName("home_banner")
public class HomeBannerPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String imageUrl;

    private Integer sort;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
