package com.human.digital.digitalhuman.service.model.response;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.service.model.dto.FileInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @Author taoHouChao
 * @Date 15:20 2025/6/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "课程详情")
public class CourseDetailDTO {

    @Schema(description = "课程id")
    private Integer id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @Schema(description = "课程图片")
    private String pictureUrl;

    @Schema(description = "教师名称")
    private String teacherName;

    @Schema(description = "教师头像")
    private String headUrl;

    @Schema(description = "课程节")
    private List<CourseNodeDTO> nodes;

    @Schema(description = "课程是否完成")
    public Boolean finished;

    @Schema(description = "状态 1不可以考 2可以考试 3考完了", example = "0")
    private Integer state;

    @Schema(description = "课程节数量")
    private Integer nodeSize;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public LocalDateTime publishTime;

    @Schema(description = "课程科目")
    public String subject;

    @Data
    @Schema(description = "课程节点")
    public static class CourseNodeDTO{

        @Schema(description = "课程节点id")
        @NotNull(message = "课程节点id不能为空")
        private Integer id;

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

        @Schema(description = "文件地址")
        private List<FileInfoDTO> files;

        @Schema(description = "x轴坐标")
        @NotEmpty(message = "x轴坐标不能为空")
        private String xAxis;

        @Schema(description = "y轴坐标")
        @NotEmpty(message = "y轴坐标不能为空")
        private String yAxis;

        @Schema(description = "是否为结束节点")
        private Boolean last;

        @Schema(description = "题目数量")
        private Integer questionCount;

        @Schema(description = "是否可考核")
        private Integer state;

        public static CourseNodeDTO of(CourseNodePO courseNodePO,
                                       Function<Integer, Boolean> lastNodeConverter,
                                       Function<String, String> urlConverter,
                                       Function<Integer, Boolean> finishedConverter){
            CourseNodeDTO courseNodeDTO = new CourseNodeDTO();
            courseNodeDTO.setId(courseNodePO.getId());
            courseNodeDTO.setNodeName(courseNodePO.getNodeName());
            courseNodeDTO.setNodeIntroduce(courseNodePO.getNodeIntroduce());
            courseNodeDTO.setQuestionCount(courseNodePO.getQuestionCount());
            courseNodeDTO.setNodeColour(courseNodePO.getNodeColour());
            if (StrUtil.isNotBlank(courseNodePO.getRelateNode())) {
                courseNodeDTO.setRelateNode(JSONUtil.toList(courseNodePO.getRelateNode(), Integer.class));
            }else {
                courseNodeDTO.setRelateNode(Collections.emptyList());
            }
            courseNodeDTO.setNodeSize(courseNodePO.getNodeSize());
            // 设置颜色
            if (!Objects.equals("开始", courseNodePO.getNodeName()) && !Objects.equals("结束", courseNodePO.getNodeName())) {
                Boolean finished = finishedConverter.apply(courseNodePO.getId());
                if (finished) {
                    courseNodeDTO.setState(1);
                }else {
                    courseNodeDTO.setState(0);
                }
            }
            if (StrUtil.isNotBlank(courseNodePO.getVideoUrl())) {
                FileInfoDTO videoInfo = JSONUtil.toBean(courseNodePO.getVideoUrl(), FileInfoDTO.class);
                videoInfo.setUrl(urlConverter.apply(videoInfo.getUrl()));
                courseNodeDTO.setVideo(videoInfo);
            }
            if (StrUtil.isNotBlank(courseNodePO.getFilesUrl())) {
                List<FileInfoDTO> fileInfos = JSONUtil.toList(courseNodePO.getFilesUrl(), FileInfoDTO.class);
                fileInfos.forEach(item -> item.setUrl(urlConverter.apply(item.getUrl())));
                courseNodeDTO.setFiles(fileInfos);
            }else {
                courseNodeDTO.setFiles(new ArrayList<>(0));
            }
            courseNodeDTO.setXAxis(courseNodePO.getXAxis());
            courseNodeDTO.setYAxis(courseNodePO.getYAxis());
            courseNodeDTO.setLast(lastNodeConverter.apply(courseNodePO.getId()));
            return courseNodeDTO;
        }
    }
}
