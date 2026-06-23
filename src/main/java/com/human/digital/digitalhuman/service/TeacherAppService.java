package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.enums.UserRoleEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.PasswordUtils;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.request.TeacherCreateCmd;
import com.human.digital.digitalhuman.service.model.request.TeacherListQuery;
import com.human.digital.digitalhuman.service.model.request.TeacherModifyCmd;
import com.human.digital.digitalhuman.service.model.response.TeacherSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Objects;

/**
 * 教师管理服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Service
@RequiredArgsConstructor
public class TeacherAppService {

    private final UserService userService;

    /**
     * 分页查询教师列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public IPage<TeacherSummaryDTO> pageQuery(TeacherListQuery query) {
        UserPO current = currentUserOrReject();

        // 确定查询的学校ID
        Integer schoolId;
        if (Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            // 超级管理员：使用前端传递的schoolId，可为空
            schoolId = query.getSchoolId();
        } else {
            // 学校管理员：只能查看本校教师；schoolId 缺失则 fail-closed，避免查询全租户
            schoolId = current.getSchoolId();
            if (schoolId == null || schoolId <= 0) {
                throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
            }
        }

        // 查询教师列表（role = 2）
        IPage<UserPO> page = userService.pageQueryTeacher(query.getUserName(), query.getAccount(), schoolId, query.getPage(), query.getSize());
        return page.convert(userPO -> BeanUtil.copyProperties(userPO, TeacherSummaryDTO.class));
    }

    /**
     * 创建教师
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    public Boolean create(TeacherCreateCmd cmd) {
        UserPO current = currentUserOrReject();
        UserPO po = cmd.toPo();
        po.setRole(UserRoleEnums.TEACHER.getCode()); // 强制教师角色
        po.setPassword(PasswordUtils.ensureEncoded(po.getPassword()));
        // 非超管强制 schoolId 为当前管理员所在学校
        if (!Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            if (current.getSchoolId() == null || current.getSchoolId() <= 0) {
                throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
            }
            po.setSchoolId(current.getSchoolId());
        }
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
     * 编辑教师
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    public Boolean modify(TeacherModifyCmd cmd) {
        // 检查教师是否存在
        UserPO existTeacher = userService.getById(cmd.getId());
        if (existTeacher == null || !Integer.valueOf(UserRoleEnums.TEACHER.getCode()).equals(existTeacher.getRole())) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        // 租户隔离：非超管只能修改本校教师
        UserPO current = currentUserOrReject();
        if (!Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            if (current.getSchoolId() == null || !Objects.equals(current.getSchoolId(), existTeacher.getSchoolId())) {
                throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
            }
        }
        String encodedPassword = StrUtil.isNotBlank(cmd.getPassword())
                ? PasswordUtils.encode(cmd.getPassword())
                : null;

        return userService.update(Wrappers.lambdaUpdate(UserPO.class)
                .set(StrUtil.isNotBlank(cmd.getUserName()), UserPO::getUserName, cmd.getUserName())
                .set(StrUtil.isNotBlank(cmd.getAccount()), UserPO::getAccount, cmd.getAccount())
                .set(StrUtil.isNotBlank(cmd.getPassword()), UserPO::getPassword, encodedPassword)
                .set(StrUtil.isNotBlank(cmd.getAvatar()), UserPO::getAvatar, cmd.getAvatar())
                .set(cmd.getState() != null, UserPO::getState, cmd.getState())
                .eq(UserPO::getId, cmd.getId())
                .eq(UserPO::getRole, UserRoleEnums.TEACHER.getCode())); // 确保只更新教师角色
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
        if (existTeacher == null || !Integer.valueOf(UserRoleEnums.TEACHER.getCode()).equals(existTeacher.getRole())) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        // 租户隔离：非超管只能删除本校教师
        UserPO current = currentUserOrReject();
        if (!Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            if (current.getSchoolId() == null || !Objects.equals(current.getSchoolId(), existTeacher.getSchoolId())) {
                throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
            }
        }
        return userService.removeById(id);
    }

    /**
     * 加载当前登录的后台用户；非后台角色（学生/教师等）拒绝。
     * 教师管理仅限超管（role=0）与学校管理员（role=1）。
     */
    private UserPO currentUserOrReject() {
        Integer uid = StpUtil.getLoginIdAsInt();
        UserPO current = userService.getById(uid);
        if (current == null) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        Integer role = current.getRole();
        boolean isBackend = Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(role)
                || Integer.valueOf(UserRoleEnums.SCHOOL_ADMIN.getCode()).equals(role);
        if (!isBackend) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        return current;
    }
}
