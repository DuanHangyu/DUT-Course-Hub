package com.human.digital.digitalhuman.service.model.response;

import com.human.digital.digitalhuman.service.model.dto.FileInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 15:06 2026/3/11
 */
@Data
@Schema(description = "课程节详情")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseNodeDetailDTO {

    @Schema(description = "课程节点id")
    private Integer id;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点介绍")
    private String nodeIntroduce;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程所属学科")
    private String subject;

    @Schema(description = "学习时长")
    private Integer studyTime;

    @Schema(description = "学习进度")
    private Integer studyProgress;

    @Schema(description = "视频地址")
    private FileInfoDTO video;

    @Schema(description = "文件地址")
    private List<FileInfoDTO> files;

    @Schema(description = "是否学过（0-未学过，1-学过）")
    private Integer state;
}
