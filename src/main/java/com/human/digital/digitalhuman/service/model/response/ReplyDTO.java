package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.human.digital.digitalhuman.service.model.dto.FileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 00:39 2025/9/13
 */
@Data
@Schema(description = "课程学生回复")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyDTO {

    @Schema(description = "提问id")
    private Integer id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户类型 0学生 1老师")
    private Integer userType;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "创建者id")
    private Integer creatorId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "文件")
    private List<FileDTO> files;

    @Schema(description = "图片")
    private List<FileDTO> pictures;

    @Schema(description = "回复人")
    private String replyName;

    @Schema(description = "回复人类型 0学生 1老师")
    private Integer replyUserType;
}
