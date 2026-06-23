package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.request.TeacherCreateCmd;
import com.human.digital.digitalhuman.service.model.request.TeacherListQuery;
import com.human.digital.digitalhuman.service.model.request.TeacherModifyCmd;
import com.human.digital.digitalhuman.service.model.request.TeacherStateChangeCmd;
import com.human.digital.digitalhuman.service.model.response.TeacherSummaryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 教师管理服务（简化版）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Service
@RequiredArgsConstructor
public class SchoolTeacherAppService {

    private final UserService userService;

    /**
     * 分页查询教师列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public IPage<TeacherSummaryDTO> pageQuery(TeacherListQuery query) {
        // 获取当前用户信息
        Integer currentUserId = StpUtil.getLoginIdAsInt();
        UserPO currentUser = userService.getById(currentUserId);

        // 确定查询的学校ID
        Integer schoolId;
        if (currentUser.getRole() == 0) {
            // 超级管理员：使用前端传递的schoolId，可为空
            schoolId = query.getSchoolId();
        } else {
            // 学校管理员：只能查看本校教师
            schoolId = currentUser.getSchoolId();
        }

        // 查询教师列表（role = 2）
        IPage<UserPO> page = userService.pageQueryTeacher(query.getUserName(), query.getAccount(), schoolId, query.getPage(), query.getSize());
        return page.convert(userPO -> BeanUtil.copyProperties(userPO, TeacherSummaryDTO.class));
    }

    /**
     * 获取教师详情
     *
     * @param id 教师ID
     * @return 教师信息
     */
    public TeacherSummaryDTO getById(Integer id) {
        UserPO userPO = userService.getById(id);
        if (userPO == null || !Integer.valueOf(2).equals(userPO.getRole())) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        return BeanUtil.copyProperties(userPO, TeacherSummaryDTO.class);
    }

    /**
     * 创建教师
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    public Boolean create(TeacherCreateCmd cmd) {
        UserPO po = cmd.toPo();
        try {
            userService.save(po);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException(ErrorCodeEnums.USER_EXIST);
            }
            throw new BusinessException(ErrorCodeEnums.USER_SAVE_ERROR);
        }
        return true;
    }

    /**
     * 更新教师
     *
     * @param cmd 更新命令
     * @return 是否成功
     */
    public Boolean update(TeacherModifyCmd cmd) {
        // 检查教师是否存在
        UserPO existTeacher = userService.getById(cmd.getId());
        if (existTeacher == null || !Integer.valueOf(2).equals(existTeacher.getRole())) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }

        return userService.update(Wrappers.lambdaUpdate(UserPO.class)
                .set(StrUtil.isNotBlank(cmd.getUserName()), UserPO::getUserName, cmd.getUserName())
                .set(StrUtil.isNotBlank(cmd.getAccount()), UserPO::getAccount, cmd.getAccount())
                .set(StrUtil.isNotBlank(cmd.getPassword()), UserPO::getPassword, cmd.getPassword())
                .set(StrUtil.isNotBlank(cmd.getAvatar()), UserPO::getAvatar, cmd.getAvatar())
                .set(cmd.getState() != null, UserPO::getState, cmd.getState())
                .eq(UserPO::getId, cmd.getId())
                .eq(UserPO::getRole, 2)); // 确保只更新教师角色
    }

    /**
     * 删除教师
     *
     * @param id 教师ID
     * @return 是否成功
     */
    public Boolean delete(Integer id) {
        // 检查教师是否存在
        UserPO existTeacher = userService.getById(id);
        if (existTeacher == null || !Integer.valueOf(2).equals(existTeacher.getRole())) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        return userService.removeById(id);
    }

    public Boolean changeState(@Valid TeacherStateChangeCmd cmd) {
        UserPO existTeacher = userService.getById(cmd.getId());
        if (existTeacher == null || !Integer.valueOf(2).equals(existTeacher.getRole())) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        return userService.update(Wrappers.lambdaUpdate(UserPO.class)
                .set(UserPO::getState, cmd.getState())
                .eq(UserPO::getId, cmd.getId())
                .eq(UserPO::getRole, 2));
    }
}
