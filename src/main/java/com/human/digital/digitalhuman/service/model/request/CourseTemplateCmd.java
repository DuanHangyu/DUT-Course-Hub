package com.human.digital.digitalhuman.service.model.request;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 模板课程命令对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "模板课程命令对象")
public class CourseTemplateCmd {

    @Schema(description = "课程ID（编辑时使用）")
    private Integer id;

    @NotBlank(message = "课程名称不能为空")
    @Schema(description = "课程名称")
    private String courseName;

    @NotBlank(message = "课程介绍不能为空")
    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @NotNull(message = "课程状态不能为空")
    @Schema(description = "课程状态")
    private Boolean state;

    @NotBlank(message = "课程封面不能为空")
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

    public CoursePO toPo() {
        CoursePO course = new CoursePO();
        course.setCourseName(courseName);
        course.setCourseIntroduce(courseIntroduce);
        course.setState(state);
        course.setPictureUrl(pictureUrl);
        course.setSubject(subject);
        course.setTeacherName(teacherName);
        course.setHeadUrl(headUrl);
        course.setCourseType(1); // 模板课程
        if (id == null) {
            course.setCreatorId(StpUtil.getLoginIdAsInt());
        }
        return course;
    }
}
