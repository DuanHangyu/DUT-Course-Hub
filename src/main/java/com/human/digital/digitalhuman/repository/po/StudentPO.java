package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author taoHouChao
 * @Date 21:24 2025/6/10
 */
@Data
@TableName("student")
public class StudentPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "班级")
    private String schoolClass;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
