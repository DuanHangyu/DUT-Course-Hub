package com.human.digital.digitalhuman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import com.human.digital.digitalhuman.repository.po.SubjectPO;
import com.human.digital.digitalhuman.repository.service.CourseService;
import com.human.digital.digitalhuman.repository.service.SubjectService;
import com.human.digital.digitalhuman.service.model.request.SubjectSortCmd;
import com.human.digital.digitalhuman.service.model.response.SubjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学科应用服务
 *
 * @Author taoHouChao
 * @Date 10:30 2026/3/10
 */
@Service
@RequiredArgsConstructor
public class SubjectAppService {

    private final SubjectService subjectService;
    private final CourseService courseService;

    /**
     * 获取学科列表（含课程数量）
     *
     * @param schoolId 学校ID
     * @return List<SubjectDTO>
     */
    public List<SubjectDTO> listWithCourseCount(Integer schoolId) {
        // 获取学科列表
        List<SubjectPO> subjectList = subjectService.listBySchoolId(schoolId);
        if (subjectList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取学科名称列表
        List<String> subjectNames = subjectList.stream()
                .map(SubjectPO::getName)
                .collect(Collectors.toList());

        // 查询课程表中对应学科的课程数量
        List<CoursePO> courses = courseService.list(new LambdaQueryWrapper<CoursePO>()
                .eq(CoursePO::getSchoolId, schoolId)
                .in(CoursePO::getSubject, subjectNames)
                .select(CoursePO::getSubject)
        );

        // 按学科名称统计课程数量
        Map<String, Long> courseCountMap = courses.stream()
                .collect(Collectors.groupingBy(CoursePO::getSubject, Collectors.counting()));

        // 构建 DTO 列表
        return subjectList.stream()
                .map(subject -> {
                    Integer count = courseCountMap.getOrDefault(subject.getName(), 0L).intValue();
                    return SubjectDTO.of(subject, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新学科排序
     *
     * @param schoolId 学校ID
     * @param cmd      排序命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSortOrder(Integer schoolId, SubjectSortCmd cmd) {
        List<Integer> subjectIds = cmd.getSubjectIds();

        // 查询所有学科
        List<SubjectPO> subjectList = subjectService.listBySchoolId(schoolId);

        // 构建 ID 到 SubjectPO 的映射
        Map<Integer, SubjectPO> subjectMap = subjectList.stream()
                .collect(Collectors.toMap(SubjectPO::getId, s -> s));

        // 按顺序构建新的列表
        List<SubjectPO> orderedList = new ArrayList<>();
        for (Integer subjectId : subjectIds) {
            SubjectPO subject = subjectMap.get(subjectId);
            if (subject != null) {
                orderedList.add(subject);
            }
        }

        // 批量更新排序
        return subjectService.batchUpdateSortOrder(orderedList);
    }
}
