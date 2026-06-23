package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级实体类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@TableName("school_class")
public class SchoolClassPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer schoolId;

    private String className;

    private Integer teacherId;

    private Integer studentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
