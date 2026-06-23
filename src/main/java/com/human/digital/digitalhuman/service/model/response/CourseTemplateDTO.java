package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * 模板课程响应对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
public class CourseTemplateDTO {

    private Integer id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @Schema(description = "课程状态")
    private Boolean state;

    @Schema(description = "课程封面图片原始URL")
    private String pictureUrl;

    @Schema(description = "课程封面图片OSS签名URL")
    private String pictureUrlSigned;

    @Schema(description = "所属学科")
    private String subject;

    @Schema(description = "授课老师")
    private String teacherName;

    @Schema(description = "教师头像原始URL")
    private String headUrl;

    @Schema(description = "教师头像OSS签名URL")
    private String headUrlSigned;

    @Schema(description = "创建人名称")
    private String createName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "课程类型：1-模板课程，2-学习课程")
    private Integer courseType;

    public static CourseTemplateDTO of(CoursePO po, Function<String, String> signedUrlConverter) {
        CourseTemplateDTO dto = new CourseTemplateDTO();
        dto.setId(po.getId());
        dto.setCourseName(po.getCourseName());
        dto.setCourseIntroduce(po.getCourseIntroduce());
        dto.setState(po.getState());
        dto.setPictureUrl(po.getPictureUrl());
        dto.setPictureUrlSigned(signedUrlConverter.apply(po.getPictureUrl()));
        dto.setSubject(po.getSubject());
        dto.setTeacherName(po.getTeacherName());
        dto.setHeadUrl(po.getHeadUrl());
        dto.setHeadUrlSigned(signedUrlConverter.apply(po.getHeadUrl()));
        dto.setCourseType(po.getCourseType());
        dto.setCreateTime(po.getCreateTime());
        return dto;
    }

    public static CourseTemplateDTO of(CoursePO po) {
        return of(po, url -> url);
    }
}
