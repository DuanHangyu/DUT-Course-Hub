package com.human.digital.digitalhuman.service.model.response;

import com.human.digital.digitalhuman.repository.po.CoursePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * @Author taoHouChao
 * @Date 15:14 2025/6/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "课程简易信息")
public class CourseSimpleDTO {

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

    @Schema(description = "是否有权限")
    private Boolean hasAuth;

    public static CourseSimpleDTO of(CoursePO coursePO, Function<String, String> urlConverter, Function<Integer, Boolean> authFunction){
        return CourseSimpleDTO.builder()
                .id(coursePO.getId())
                .courseName(coursePO.getCourseName())
                .teacherName(coursePO.getTeacherName())
                .headUrl(urlConverter.apply(coursePO.getHeadUrl()))
                .courseIntroduce(coursePO.getCourseIntroduce())
                .pictureUrl(urlConverter.apply(coursePO.getPictureUrl()))
                .hasAuth(authFunction.apply(coursePO.getId()))
                .build();
    }
}
