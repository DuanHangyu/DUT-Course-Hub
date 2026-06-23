package com.human.digital.digitalhuman.service.model.request;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 校本课程命令对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "校本课程命令对象")
public class SchoolCourseCmd {

    @Schema(description = "课程ID（编辑时使用）")
    private Integer id;

    @NotBlank(message = "课程名称不能为空")
    @Schema(description = "课程名称")
    private String courseName;

    @NotBlank(message = "课程介绍不能为空")
    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @NotNull(message = "课程状态不能为空")
    @Schema(description = "课程状态（true-上架，false-下架）")
    private Boolean state;

    @Schema(description = "是否精选（0-否，1-是）")
    private Integer isFeatured;

    @Schema(description = "课程封面图片URL")
    private String pictureUrl;

    @NotBlank(message = "所属学科不能为空")
    @Schema(description = "所属学科")
    private String subject;

    @NotBlank(message = "授课老师不能为空")
    @Schema(description = "授课老师")
    private String teacherName;

    @NotBlank(message = "教师头像不能为空")
    @Schema(description = "教师头像URL")
    private String headUrl;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "关联老师ID列表")
    private List<Integer> teacherIds;

    @Schema(description = "模版课程ID（从模版创建时使用）")
    private Integer relationCourseId;

    /**
     * 转换为课程持久化对象
     *
     * @return 课程持久化对象
     */
    public CoursePO toPo() {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setCourseName(courseName);
        course.setCourseIntroduce(courseIntroduce);
        course.setState(state);
        course.setIsFeatured(isFeatured);
        course.setPictureUrl(pictureUrl);
        course.setSubject(subject);
        course.setTeacherName(teacherName);
        course.setHeadUrl(headUrl);
        course.setSchoolId(schoolId);
        course.setInviteCode(inviteCode);
        course.setCourseType(2); // 校本课程
        if (id == null) {
            course.setCreatorId(StpUtil.getLoginIdAsInt());
        }
        return course;
    }
}
