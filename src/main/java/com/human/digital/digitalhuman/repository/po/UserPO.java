package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author taoHouChao
 * @Date 16:29 2025/6/9
 */
@Data
@TableName("user")
public class UserPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String account;

    private String userName;

    private String phone;

    private String password;

    private Boolean state;

    /**
     * 角色：0-超级管理员，1-学校管理员，2-老师
     */
    private Integer role;

    /**
     * 所属学校 超级管理员的school_id=-1
     */
    private Integer schoolId;

    /**
     * 头像URL
     */
    private String avatar;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
