package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校实体类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Data
@TableName("school")
public class SchoolPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String schoolName;

    private String principalName;

    private String phone;

    private String location;

    private Integer state;

    private String domain;

    private Integer accessPolicy;

    private String ipWhitelist;

    private String logoUrl;

    private String authModules;

    private Integer maxStudents;

    private Integer aiTokens;

    private Integer adminUserId;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String latitude;

    private String longitude;
}
