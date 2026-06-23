package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学科实体类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/10
 */
@Data
@TableName("subject")
public class SubjectPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学科名称
     */
    private String name;

    /**
     * 排序权重，数值越小越靠前
     */
    private Integer sortOrder;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
