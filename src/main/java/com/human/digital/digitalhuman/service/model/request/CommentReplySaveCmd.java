package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.service.model.dto.FileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 23:58 2025/9/12
 */
@Data
@Schema(description = "课程学生回复保存命令")
public class CommentReplySaveCmd {

    @Schema(description = "课程id")
    private Integer courseId;

    @Schema(description = "课程节点id")
    private Integer courseNodeId;

    @Schema(description = "评论id")
    private Integer commentId;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "文件")
    private List<FileDTO> files;

    @Schema(description = "图片")
    private List<FileDTO> pictures;
}
