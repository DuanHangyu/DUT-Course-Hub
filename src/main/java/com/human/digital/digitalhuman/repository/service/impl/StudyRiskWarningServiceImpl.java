package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.StudyRiskWarningMapper;
import com.human.digital.digitalhuman.repository.po.StudyRiskWarningPO;
import com.human.digital.digitalhuman.repository.service.StudyRiskWarningService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学业风险预警服务实现类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Service
public class StudyRiskWarningServiceImpl extends ServiceImpl<StudyRiskWarningMapper, StudyRiskWarningPO>
        implements StudyRiskWarningService {

    @Override
    public List<StudyRiskWarningPO> listByCourseId(Long courseId) {
        return this.list(Wrappers.lambdaQuery(StudyRiskWarningPO.class)
                .eq(StudyRiskWarningPO::getCourseId, courseId)
                .orderByDesc(StudyRiskWarningPO::getCreateTime));
    }

    @Override
    public boolean handleWarning(Long id, Long handlerId) {
        StudyRiskWarningPO warning = new StudyRiskWarningPO();
        warning.setId(id);
        warning.setHandlerId(handlerId);
        warning.setHandleTime(LocalDateTime.now());
        warning.setStatus(1);
        return this.updateById(warning);
    }

    @Override
    public List<StudyRiskWarningPO> listUnprocessedByStudentAndCourse(Long studentId, Long courseId) {
        return this.list(Wrappers.lambdaQuery(StudyRiskWarningPO.class)
                .eq(StudyRiskWarningPO::getStudentId, studentId)
                .eq(StudyRiskWarningPO::getCourseId, courseId)
                .eq(StudyRiskWarningPO::getStatus, 0));
    }
}
