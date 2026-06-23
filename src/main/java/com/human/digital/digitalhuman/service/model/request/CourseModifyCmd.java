package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 22:24 2025/6/11
 */
@Data
@Schema(description = "修改课程请求")
public class CourseModifyCmd {

    @Schema(description = "课程id")
    @NotNull(message = "课程id不能为空")
    private Integer id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教师名称")
    private String teacherName;

    @Schema(description = "教师头像")
    private String headUrl;

    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @Schema(description = "课程状态")
    private Boolean state;

    @Schema(description = "课程图片")
    private String pictureUrl;

    @Schema(description = "终极答辩题目数量")
    private Integer lastDefenseQuestionsCount;

    @Schema(description = "邀请教师id集合")
    private List<Integer> inviteTeachers;

    @Schema(description = "班级名称集合")
    private List<SchoolStudentDTO> schoolClassList;

    @Schema(description = "学生id集合")
    private List<Integer> studentIds;

    public Integer generateRelationType(){
        int relationType = 0;
        if (CollectionUtils.isNotEmpty(studentIds) && CollectionUtils.isNotEmpty(schoolClassList)) {
            relationType = 2;
        }else if (CollectionUtils.isNotEmpty(schoolClassList)) {
            relationType = 1;
        }
        return relationType;
    }
}
