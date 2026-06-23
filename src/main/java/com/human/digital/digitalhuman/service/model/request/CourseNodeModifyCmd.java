package com.human.digital.digitalhuman.service.model.request;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.service.model.dto.FileInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 18:38 2025/6/12
 */
@Data
@Schema(description = "修改课程节请求")
public class CourseNodeModifyCmd {

    @Schema(description = "课程节点id")
    @NotNull(message = "课程节点id不能为空")
    private Integer id;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点介绍")
    private String nodeIntroduce;

    @Schema(description = "关联节点")
    @JsonProperty("relateNode")
    private List<Integer> relateNode;

    @Schema(description = "节点大小")
    private Integer nodeSize;

    @Schema(description = "节点颜色")
    private String nodeColour;

    @Schema(description = "视频地址")
    @JsonProperty("video")
    private FileInfoDTO video;

    @Schema(description = "视频时长")
    private Integer videoDuration;

    @Schema(description = "文件地址")
    @JsonProperty("files")
    private List<FileInfoDTO> files;

    @Schema(description = "及格线")
    private Integer passLine;

    @Schema(description = "节点提问数量")
    private Integer questionCount;

    public CourseNodePO toPo() {
        CourseNodePO courseNodePO = new CourseNodePO();
        courseNodePO.setId(id);
        courseNodePO.setNodeName(nodeName);
        courseNodePO.setNodeIntroduce(nodeIntroduce);
        courseNodePO.setRelateNode(JSONUtil.toJsonStr(relateNode));
        courseNodePO.setNodeSize(nodeSize);
        courseNodePO.setNodeColour(nodeColour);
        courseNodePO.setVideoUrl(JSONUtil.toJsonStr(video));
        courseNodePO.setFilesUrl(JSONUtil.toJsonStr(files));
        courseNodePO.setCreatorId(StpUtil.getLoginIdAsInt());
        courseNodePO.setPassLine(passLine);
        courseNodePO.setQuestionCount(questionCount);
        return courseNodePO;
    }
}
