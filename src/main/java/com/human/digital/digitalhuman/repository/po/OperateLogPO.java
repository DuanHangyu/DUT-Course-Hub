package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @Author taoHouChao
 * @Date 10:32 2026/3/13
 */
@Data
@TableName("operate_log")
public class OperateLogPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

}
