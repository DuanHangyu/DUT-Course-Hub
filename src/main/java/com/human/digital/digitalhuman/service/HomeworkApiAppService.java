package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.CourseStudentRelationPO;
import com.human.digital.digitalhuman.repository.po.HomeworkPO;
import com.human.digital.digitalhuman.repository.po.HomeworkSubmitPO;
import com.human.digital.digitalhuman.repository.service.CourseStudentRelationService;
import com.human.digital.digitalhuman.repository.service.HomeworkService;
import com.human.digital.digitalhuman.repository.service.HomeworkSubmitService;
import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import com.human.digital.digitalhuman.service.model.request.HomeworkSubmitCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkSubmitDeleteCmd;
import com.human.digital.digitalhuman.service.model.response.HomeworkDetailDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkFileDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkStudentListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业管理业务逻辑（学生端）
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeworkApiAppService {

    private final HomeworkService homeworkService;
    private final HomeworkSubmitService homeworkSubmitService;
    private final CourseStudentRelationService courseStudentRelationService;
    private final OssClientUtils ossClientUtils;

    /**
     * 查询课程作业列表
     *
     * @param courseId 课程ID
     * @return 作业列表
     */
    public List<HomeworkStudentListDTO> list(Integer courseId) {
        // 获取当前登录学生ID
        Integer studentId = StpUtil.getLoginIdAsInt();

        // 校验学生是否属于该课程
        boolean belongs = checkStudentBelongsToCourse(courseId, studentId);
        if (!belongs) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        // 查询课程下所有未删除的作业
        List<HomeworkPO> homeworkList = homeworkService.listByCourseId(courseId);
        if (CollectionUtil.isEmpty(homeworkList)) {
            return new ArrayList<>();
        }

        // 查询学生的提交记录
        List<Integer> homeworkIds = homeworkList.stream().map(HomeworkPO::getId).toList();
        List<HomeworkSubmitPO> submitList = homeworkSubmitService.listByStudentIdAndHomeworkIds(studentId, homeworkIds);
        Map<Integer, HomeworkSubmitPO> submitMap = submitList.stream()
                .collect(Collectors.toMap(HomeworkSubmitPO::getHomeworkId, s -> s));

        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 转换为 DTO
        return homeworkList.stream()
                .map(h -> {
                    HomeworkStudentListDTO dto = new HomeworkStudentListDTO();
                    dto.setId(h.getId());
                    dto.setTitle(h.getTitle());
                    dto.setDescription(h.getDescription());
                    dto.setDeadline(h.getDeadline());

                    // 计算提交状态
                    HomeworkSubmitPO submit = submitMap.get(h.getId());
                    if (h.getDeadline().isBefore(now)) {
                        dto.setSubmitStatus(2); // 已截止
                    } else if (submit != null) {
                        dto.setSubmitStatus(1); // 已提交
                        dto.setSubmitTime(submit.getSubmitTime());
                    } else {
                        dto.setSubmitStatus(0); // 未提交
                    }

                    return dto;
                })
                .toList();
    }

    /**
     * 查询作业详情
     *
     * @param homeworkId 作业ID
     * @return 作业详情
     */
    public HomeworkDetailDTO detail(Integer homeworkId) {
        // 获取当前登录学生ID
        Integer studentId = StpUtil.getLoginIdAsInt();

        // 校验作业是否存在
        HomeworkPO homework = homeworkService.getById(homeworkId);
        if (homework == null || Objects.equals(homework.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_EXIST);
        }

        // 校验学生是否有权限查看
        boolean belongs = checkStudentBelongsToCourse(homework.getCourseId(), studentId);
        if (!belongs) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        // 查询学生的提交记录
        HomeworkSubmitPO submit = homeworkSubmitService.getByHomeworkIdAndStudentId(homeworkId, studentId);

        // 构建返回结果
        HomeworkDetailDTO dto = new HomeworkDetailDTO();
        dto.setId(homework.getId());
        dto.setSubmitId(submit == null ? null : submit.getId());
        dto.setTitle(homework.getTitle());
        dto.setDescription(homework.getDescription());
        dto.setDeadline(homework.getDeadline());

        // 解析附件
        if (StrUtil.isNotBlank(homework.getAttachments())) {
            List<UploadDTO> attachments = JSONUtil.toList(homework.getAttachments(), UploadDTO.class);
            dto.setAttachments(attachments.stream()
                    .map(item -> HomeworkFileDTO.from(item, ossClientUtils::getSignedUrl))
                    .toList());
        }

        // 计算提交状态
        LocalDateTime now = LocalDateTime.now();
        if (homework.getDeadline().isBefore(now)) {
            dto.setSubmitStatus(2); // 已截止
        } else if (submit != null) {
            dto.setSubmitStatus(1); // 已提交
            dto.setMySubmitTime(submit.getSubmitTime());
            // 设置批阅状态和分数
            if (submit.getGradeTime() != null) {
                dto.setReviewStatus(1); // 已批阅
                dto.setScore(submit.getScore());
            } else {
                dto.setReviewStatus(0); // 未批阅
            }
            if (StrUtil.isNotBlank(submit.getFiles())) {
                List<UploadDTO> files = JSONUtil.toList(submit.getFiles(), UploadDTO.class);
                dto.setMyFiles(files.stream()
                        .map(item -> HomeworkFileDTO.from(item, ossClientUtils::getSignedUrl))
                        .toList());
            }
        } else {
            dto.setSubmitStatus(0); // 未提交
        }

        return dto;
    }

    /**
     * 提交作业
     *
     * @param cmd 提交请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(HomeworkSubmitCmd cmd) {
        // 获取当前登录学生ID
        Integer studentId = StpUtil.getLoginIdAsInt();

        // 校验作业是否存在
        HomeworkPO homework = homeworkService.getById(cmd.getHomeworkId());
        if (homework == null || Objects.equals(homework.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_EXIST);
        }

        // 校验是否已过截止时间
        if (homework.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_DEADLINE_PASSED);
        }

        // 校验学生是否属于该课程
        boolean belongs = checkStudentBelongsToCourse(homework.getCourseId(), studentId);
        if (!belongs) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        // 查询是否已有提交记录
        HomeworkSubmitPO existSubmit = homeworkSubmitService.getByHomeworkIdAndStudentId(cmd.getHomeworkId(), studentId);

        if (existSubmit != null) {
            // 更新提交记录
            existSubmit.setFiles(JSONUtil.toJsonStr(cmd.getFiles()));
            existSubmit.setSubmitTime(LocalDateTime.now());
            homeworkSubmitService.updateById(existSubmit);
            log.info("更新作业提交成功，作业ID: {}, 学生ID: {}", cmd.getHomeworkId(), studentId);
        } else {
            // 新建提交记录
            HomeworkSubmitPO newSubmit = new HomeworkSubmitPO();
            newSubmit.setHomeworkId(cmd.getHomeworkId());
            newSubmit.setStudentId(studentId);
            newSubmit.setFiles(JSONUtil.toJsonStr(cmd.getFiles()));
            newSubmit.setSubmitTime(LocalDateTime.now());
            newSubmit.setDeleted(0);
            homeworkSubmitService.save(newSubmit);
            log.info("创建作业提交成功，作业ID: {}, 学生ID: {}", cmd.getHomeworkId(), studentId);
        }

        return true;
    }

    /**
     * 查询我的提交记录
     *
     * @param homeworkId 作业ID
     * @return 提交详情
     */
    public HomeworkDetailDTO mySubmit(Integer homeworkId) {
        return detail(homeworkId);
    }

    /**
     * 校验学生是否属于该课程
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 是否属于
     */
    private boolean checkStudentBelongsToCourse(Integer courseId, Integer studentId) {
        return courseStudentRelationService.count(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId)
                .eq(CourseStudentRelationPO::getStudentId, studentId)) > 0;
    }

    public Boolean delete(HomeworkSubmitDeleteCmd deleteCmd) {
        homeworkSubmitService.removeById(deleteCmd.getId());
        return true;
    }
}
