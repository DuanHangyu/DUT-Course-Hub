package com.human.digital.digitalhuman.service.manager;

import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.repository.service.*;
import com.human.digital.digitalhuman.service.CourseAppService;
import com.human.digital.digitalhuman.service.model.response.StudentCourseDetailDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 18:55 2025/9/13
 */
@Component
@RequiredArgsConstructor
public class StudentAssessManager {

    private final CourseAppService courseAppService;

    private final CourseService courseService;

    private final CourseNodeService courseNodeService;

    private final StudentCourseNodeStudyRecordService studyRecordService;

    private final StudentCourseAssessDetailService studentCourseAssessDetailService;

    private final StudentCourseAssessService studentCourseAssessService;

    public StudentCourseDetailDTO buildFromRelation(Integer studentId, CourseStudentRelationPO relation) {
        CoursePO course = courseService.getById(relation.getCourseId());
        if (course == null) {
            return null;
        }
        List<CourseNodePO> courseNodes = courseNodeService.queryByCourseIdWithoutContent(course.getId());
        return StudentCourseDetailDTO.builder()
                .courseId(relation.getCourseId())
                .courseName(course.getCourseName())
                .tableView(StudentCourseDetailDTO.TableViewDTO.builder()
                        .studyNodeList(courseNodes.parallelStream()
                                .filter(item -> !item.startNode() && !item.endNode())
                                .map(courseNode -> buildFromCourseNode(studentId, courseNode)).toList())
                        .build())
                .nodeView(courseAppService.courseDetail(relation.getCourseId(), studentId))
                .build();
    }

    private StudentCourseDetailDTO.StudyNodeDTO buildFromCourseNode(Integer studentId, CourseNodePO item) {
        List<StudentCourseNodeStudyRecordPO> studyRecords = studyRecordService.findByCourseNodeId(studentId, item.getId());
        int state;
        if (CollectionUtils.isEmpty(studyRecords)) {
            state = 0;
        } else {
            StudentCourseAssessPO courseNodeAssess = studentCourseAssessService.findCourseNodeAssess(item.getId(), studentId);
            if (courseNodeAssess == null) {
                state = 1;
            } else {
                state = 2;
            }
        }
        int sum = studyRecords.stream().mapToInt(StudentCourseNodeStudyRecordPO::getStudyTime).sum();
        return StudentCourseDetailDTO.StudyNodeDTO.builder()
                .nodeName(item.getNodeName())
                .state(state)
                .studyTime(sum)
                .build();
    }
}
