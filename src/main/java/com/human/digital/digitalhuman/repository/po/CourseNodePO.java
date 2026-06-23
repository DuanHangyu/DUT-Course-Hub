package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author taoHouChao
 * @Date 19:28 2025/6/11
 */
@Data
@TableName("course_node")
public class CourseNodePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer courseId;

    private String nodeName;

    private String nodeIntroduce;

    private String relateNode;

    private Integer nodeSize;

    private String nodeColour;

    private String videoUrl;

    private Integer videoDuration;

    private String taskId;

    /**
     * 0待处理、1处理中、2已完成、3失败
     */
    private String taskStatus;

    private String content;

    private String videoToTextErrorMessage;

    private String filesUrl;

    private Integer passLine;

    private String xAxis;

    private String yAxis;

    private Integer questionCount;

    private Integer creatorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public boolean startNode(){
        return Objects.equals("开始", this.nodeName);
    }

    public boolean endNode(){
        return Objects.equals("结束", this.nodeName);
    }
}
