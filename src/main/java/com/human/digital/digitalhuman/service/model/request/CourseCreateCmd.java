package com.human.digital.digitalhuman.service.model.request;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 19:34 2025/6/11
 */
@Data
@Schema(description = "创建课程请求")
public class CourseCreateCmd {

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @Schema(description = "课程状态")
    private Boolean state;

    @Schema(description = "课程图片")
    private String pictureUrl;

    @Schema(description = "教师名称")
    private String teacherName;

    @Schema(description = "教师头像")
    private String headUrl;

    @Schema(description = "终极答辩题目数量")
    private Integer lastDefenseQuestionsCount;

    @Schema(description = "邀请教师id集合")
    private List<Integer> inviteTeachers;

    @Schema(description = "班级名称集合")
    private List<SchoolStudentDTO> schoolClassList;

    @Schema(description = "学生id集合")
    private List<Integer> studentIds;


    public CoursePO toPo() {
        int relationType = 0;
        if (CollectionUtils.isNotEmpty(studentIds) && CollectionUtils.isNotEmpty(schoolClassList)) {
            relationType = 2;
        }else if (CollectionUtils.isNotEmpty(schoolClassList)) {
            relationType = 1;
        }
        CoursePO course = new CoursePO();
        course.setCourseName(courseName);
        course.setCourseIntroduce(courseIntroduce);
        course.setState(state);
        course.setTeacherName(teacherName);
        course.setHeadUrl(headUrl);
        course.setPictureUrl(pictureUrl);
        course.setCreatorId(StpUtil.getLoginIdAsInt());
        course.setLastDefenseQuestionsCount(lastDefenseQuestionsCount);
        course.setRelationType(relationType);
        return course;
    }
}
