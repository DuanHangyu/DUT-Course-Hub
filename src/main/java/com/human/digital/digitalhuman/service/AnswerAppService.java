package com.human.digital.digitalhuman.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.repository.po.StudentQuestionAnswerRecordPO;
import com.human.digital.digitalhuman.repository.service.StudentQuestionAnswerRecordService;
import com.human.digital.digitalhuman.service.model.dto.QuestionSummaryDTO;
import com.human.digital.digitalhuman.service.model.response.QuestionAndAnswerDTO;
import com.human.digital.digitalhuman.service.model.response.QuestionAnswerLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @USER taoHouChao
 * @DATE 09:35 2025/6/19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerAppService {

    private final StudentQuestionAnswerRecordService studentQuestionAnswerRecordService;

    public Integer saveLog(Integer studentId,
                           String question,
                           String sessionId,
                           Integer courseId,
                           Integer courseNodeId) {
        StudentQuestionAnswerRecordPO recordPO = new StudentQuestionAnswerRecordPO();
        recordPO.setStudentId(studentId);
        recordPO.setQuestion(question);
        recordPO.setCourseId(courseId);
        recordPO.setCourseNodeId(courseNodeId);
        recordPO.setUuid(sessionId);
        studentQuestionAnswerRecordService.save(recordPO);
        return recordPO.getId();
    }

    public void updateLogAnswer(Integer logId,
                                String answer) {
        studentQuestionAnswerRecordService.update(Wrappers.lambdaUpdate(StudentQuestionAnswerRecordPO.class)
                .set(StudentQuestionAnswerRecordPO::getAnswer, answer)
                .eq(StudentQuestionAnswerRecordPO::getId, logId));
    }

    public List<QuestionAnswerLogDTO> record(Integer studentId) {
        List<QuestionAnswerLogDTO> result = new ArrayList<>();
        Map<String, List<StudentQuestionAnswerRecordPO>> resultMap = new LinkedHashMap<>();
        List<StudentQuestionAnswerRecordPO> records = studentQuestionAnswerRecordService.queryByStudentId(studentId);
        LocalDate now = LocalDate.now();
        for (StudentQuestionAnswerRecordPO record : records) {
            String day = record.getDay(now);
            if (day == null) {
                continue; // 跳过无效记录
            }
            resultMap.computeIfAbsent(day, k -> new ArrayList<>()).add(record);
        }
        for (Map.Entry<String, List<StudentQuestionAnswerRecordPO>> entry : resultMap.entrySet()) {
            String key = entry.getKey();
            List<StudentQuestionAnswerRecordPO> value = entry.getValue();
            List<QuestionAndAnswerDTO> list = value.stream()
                    .map(item -> QuestionAndAnswerDTO.builder()
                            .question(item.getQuestion())
                            .answer(item.getAnswer())
                            .papers(StrUtil.isBlank(item.getPapers()) ? null : JSONUtil.toBean(item.getPapers(), QuestionSummaryDTO.class))
                            .build())
                    .toList();
            QuestionAnswerLogDTO dto = new QuestionAnswerLogDTO();
            dto.setName(key);
            dto.setRecords(list);
            result.add(dto);
        }
        return result;
    }
}
