package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudyProgressSnapshotPO;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习进度快照服务接口
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
public interface StudyProgressSnapshotService extends IService<StudyProgressSnapshotPO> {

    /**
     * 批量保存进度快照
     *
     * @param snapshotList 快照列表
     */
    void saveBatch(List<StudyProgressSnapshotPO> snapshotList);

    /**
     * 根据课程ID和日期查询快照
     *
     * @param courseId     课程ID
     * @param snapshotDate 快照日期
     * @return 快照列表
     */
    List<StudyProgressSnapshotPO> listByCourseIdAndDate(Long courseId, LocalDate snapshotDate);

    /**
     * 获取某课程某学生的最新快照
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 快照
     */
    StudyProgressSnapshotPO getLatestByCourseAndStudent(Long courseId, Long studentId);

    /**
     * 批量获取某课程多个学生的最新快照
     *
     * @param courseId   课程ID
     * @param studentIds 学生ID列表
     * @return 快照列表
     */
    List<StudyProgressSnapshotPO> listLatestByCourseAndStudentIds(Long courseId, List<Long> studentIds);
}
