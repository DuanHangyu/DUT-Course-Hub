package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudentQuestionAnswerRecordPO;

import java.util.List;

public interface StudentQuestionAnswerRecordService extends IService<StudentQuestionAnswerRecordPO> {
    /**
     * 根据学生ID查询问题答案记录
     * @param studentId 学生ID
     * @return 问题答案记录列表
     */
    List<StudentQuestionAnswerRecordPO> queryByStudentId(int studentId);
}
