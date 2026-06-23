package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @USER taoHouChao
 * @DATE 19:51 2025/9/11
 */
@Data
@TableName("course_invite")
@Schema(description = "邀请教师")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseInvitePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "课程id")
    private Integer courseId;

    @Schema(description = "被邀请教师id")
    private Integer inviteeTeacherId;

    @Schema(description = "邀请教师id")
    private Integer InviterTeacherId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
