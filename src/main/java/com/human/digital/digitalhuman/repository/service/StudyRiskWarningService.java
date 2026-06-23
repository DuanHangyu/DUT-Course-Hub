package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudyRiskWarningPO;

import java.util.List;

/**
 * 学业风险预警服务接口
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
public interface StudyRiskWarningService extends IService<StudyRiskWarningPO> {

    /**
     * 根据课程ID查询风险预警列表
     *
     * @param courseId 课程ID
     * @return 风险预警列表
     */
    List<StudyRiskWarningPO> listByCourseId(Long courseId);

    /**
     * 处理风险预警
     *
     * @param id        预警ID
     * @param handlerId 处理人ID
     * @return 是否成功
     */
    boolean handleWarning(Long id, Long handlerId);

    /**
     * 根据学生ID和课程ID查询未处理的预警
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 预警列表
     */
    List<StudyRiskWarningPO> listUnprocessedByStudentAndCourse(Long studentId, Long courseId);
}
