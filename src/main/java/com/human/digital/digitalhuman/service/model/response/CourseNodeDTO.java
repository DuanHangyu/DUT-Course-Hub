package com.human.digital.digitalhuman.service.model.response;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.service.model.dto.FileInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author taoHouChao
 * @Date 22:46 2025/6/11
 */
@Data
@Schema(description = "课程节信息")
public class CourseNodeDTO {

    @Schema(description = "课程节点id")
    private Integer id;

    @Schema(description = "课程id")
    private Integer courseId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点介绍")
    private String nodeIntroduce;

    @Schema(description = "关联节点")
    private List<Integer> relateNode;

    @Schema(description = "节点大小")
    private Integer nodeSize;

    @Schema(description = "节点颜色")
    private String nodeColour;

    @Schema(description = "视频地址")
    private FileInfoDTO video;

    @Schema(description = "视频时长")
    private Integer videoDuration;

    @Schema(description = "文件地址")
    private List<FileInfoDTO> files;

    @Schema(description = "x轴坐标")
    private String xAxis;

    @Schema(description = "y轴坐标")
    private String yAxis;

    public static CourseNodeDTO of(CourseNodePO po) {
        CourseNodeDTO courseNodeDTO = new CourseNodeDTO();
        courseNodeDTO.setId(po.getId());
        courseNodeDTO.setCourseId(po.getCourseId());
        courseNodeDTO.setNodeName(po.getNodeName());
        courseNodeDTO.setNodeIntroduce(po.getNodeIntroduce());
        if (StrUtil.isNotBlank(po.getRelateNode())) {
            courseNodeDTO.setRelateNode(JSONUtil.toList(po.getRelateNode(), Integer.class));
        }else {
            courseNodeDTO.setRelateNode(new ArrayList<>(0));
        }
        courseNodeDTO.setNodeSize(po.getNodeSize());
        courseNodeDTO.setNodeColour(po.getNodeColour());
        if (StrUtil.isNotBlank(po.getVideoUrl())) {
            courseNodeDTO.setVideo(JSONUtil.toBean(po.getVideoUrl(), FileInfoDTO.class));
            courseNodeDTO.setVideoDuration(po.getVideoDuration());
        }
        if (StrUtil.isNotBlank(po.getFilesUrl())) {
            courseNodeDTO.setFiles(JSONUtil.toList(po.getFilesUrl(), FileInfoDTO.class));
        }else {
            courseNodeDTO.setFiles(new ArrayList<>(0));
        }
        courseNodeDTO.setXAxis(po.getXAxis());
        courseNodeDTO.setYAxis(po.getYAxis());
        return courseNodeDTO;
    }
}
