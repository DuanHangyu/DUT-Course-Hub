package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.StudentQuestionAnswerRecordMapper;
import com.human.digital.digitalhuman.repository.po.StudentQuestionAnswerRecordPO;
import com.human.digital.digitalhuman.repository.service.StudentQuestionAnswerRecordService;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 09:31 2025/6/19
 */
@Repository
public class StudentQuestionAnswerRecordServiceImpl extends ServiceImpl<StudentQuestionAnswerRecordMapper, StudentQuestionAnswerRecordPO> implements StudentQuestionAnswerRecordService {
    @Override
    public List<StudentQuestionAnswerRecordPO> queryByStudentId(int studentId) {
        return this.list(Wrappers.lambdaQuery(StudentQuestionAnswerRecordPO.class)
                .eq(StudentQuestionAnswerRecordPO::getStudentId, studentId)
                .orderByDesc(StudentQuestionAnswerRecordPO::getId));
    }
}
