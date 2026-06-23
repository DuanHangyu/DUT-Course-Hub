package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页底图实体类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Data
@TableName("home_background")
public class HomeBackgroundPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String imageUrl;

    private String jumpUrl;

    private Integer sort;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
