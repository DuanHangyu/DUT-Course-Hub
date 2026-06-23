package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.HomeworkSubmitPO;

import java.util.List;

/**
 * 作业提交服务接口
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
public interface HomeworkSubmitService extends IService<HomeworkSubmitPO> {

    /**
     * 根据作业ID查询提交列表
     *
     * @param homeworkId 作业ID
     * @return 提交列表
     */
    List<HomeworkSubmitPO> listByHomeworkId(Integer homeworkId);

    /**
     * 根据作业ID和学生ID查询提交记录
     *
     * @param homeworkId 作业ID
     * @param studentId  学生ID
     * @return 提交记录
     */
    HomeworkSubmitPO getByHomeworkIdAndStudentId(Integer homeworkId, Integer studentId);

    /**
     * 根据学生ID和作业ID列表查询提交记录
     *
     * @param studentId   学生ID
     * @param homeworkIds 作业ID列表
     * @return 提交记录列表
     */
    List<HomeworkSubmitPO> listByStudentIdAndHomeworkIds(Integer studentId, List<Integer> homeworkIds);
}
