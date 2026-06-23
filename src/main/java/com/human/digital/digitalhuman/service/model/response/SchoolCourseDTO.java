package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * 校本课程响应对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchoolCourseDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeacherInfo {
        @Schema(description = "老师ID")
        private Integer id;

        @Schema(description = "老师姓名")
        private String userName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentInfo{

        @Schema(description = "学生ID")
        private Integer id;

        @JsonIgnore
        private Integer relationType;

        @JsonIgnore
        private Integer schoolClassId;

        @Schema(description = "学生姓名")
        private String studentName;
    }

    @Data
    @Schema(description = "学校班级信息")
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SchoolClassInfo{

        @Schema(description = "班级ID")
        private Integer id;

        @Schema(description = "班级名称")
        private String schoolClassName;
    }

    private Integer id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @Schema(description = "课程状态（true-上架，false-下架）")
    private Boolean state;

    @Schema(description = "是否精选（0-否，1-是）")
    private Integer isFeatured;

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

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "学生人数")
    private Integer studentCount;

    @Schema(description = "创建人名称")
    private String createName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "关联老师列表")
    private List<TeacherInfo> teachers;

    @Schema(description = "关联学生列表")
    private List<StudentInfo> students;

    @Schema(description = "关联班级列表")
    private List<SchoolClassInfo> schoolClasses;

    public static SchoolCourseDTO of(CoursePO po, Function<String, String> signedUrlConverter) {
        SchoolCourseDTO dto = new SchoolCourseDTO();
        dto.setId(po.getId());
        dto.setCourseName(po.getCourseName());
        dto.setCourseIntroduce(po.getCourseIntroduce());
        dto.setState(po.getState());
        dto.setIsFeatured(po.getIsFeatured());
        dto.setPictureUrl(po.getPictureUrl());
        dto.setPictureUrlSigned(signedUrlConverter.apply(po.getPictureUrl()));
        dto.setSubject(po.getSubject());
        dto.setTeacherName(po.getTeacherName());
        dto.setHeadUrl(po.getHeadUrl());
        dto.setHeadUrlSigned(signedUrlConverter.apply(po.getHeadUrl()));
        dto.setSchoolId(po.getSchoolId());
        dto.setInviteCode(po.getInviteCode());
        dto.setStudentCount(po.getStudentCount());
        dto.setCreateTime(po.getCreateTime());
        return dto;
    }

    public static SchoolCourseDTO of(CoursePO po) {
        return of(po, url -> url);
    }
}
