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
import com.human.digital.digitalhuman.repository.po.SchoolPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.SchoolService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.request.UserCreateCmd;
import com.human.digital.digitalhuman.service.model.request.UserListQuery;
import com.human.digital.digitalhuman.service.model.request.UserModifyCmd;
import com.human.digital.digitalhuman.service.model.response.SchoolSimpleDTO;
import com.human.digital.digitalhuman.service.model.response.UserDetailDTO;
import com.human.digital.digitalhuman.service.model.response.UserSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Author taoHouChao
 * @Date 14:28 2025/6/11
 */
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserService userService;

    private final SchoolService schoolService;

    public Boolean create(UserCreateCmd cmd) {
        UserPO current = currentUserOrReject();
        UserPO po = cmd.toPo();
        po.setPassword(PasswordUtils.ensureEncoded(po.getPassword()));
        // 非超管：强制写入当前学校，且禁止越权创建超管/跨校账号
        if (!Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            if (current.getSchoolId() == null || current.getSchoolId() <= 0) {
                throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
            }
            po.setSchoolId(current.getSchoolId());
            po.setRole(UserRoleEnums.SCHOOL_ADMIN.getCode()); // 学校管理员只能创建同校管理员
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

    public IPage<UserSummaryDTO> pageQuery(UserListQuery query) {
        // 租户隔离：从 DB 读取当前用户权威角色/schoolId，避免 session 被篡改或缺失时绕过。
        UserPO current = currentUserOrReject();
        Integer schoolId = Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())
                ? null
                : current.getSchoolId();
        if (schoolId == null && !Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            // 非超管但 schoolId 缺失：拒绝，避免数据泄露
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        IPage<UserPO> page = userService.pageQuery(query.getUserName(), query.getAccount(), query.getState(), schoolId, query.getPage(), query.getSize());
        return page.convert(userPO -> BeanUtil.copyProperties(userPO, UserSummaryDTO.class));
    }

    public Boolean modify(UserModifyCmd cmd) {
        assertTenantAccess(cmd.getId());
        String encodedPassword = StrUtil.isNotBlank(cmd.getPassword())
                ? PasswordUtils.encode(cmd.getPassword())
                : null;
        return userService.update(Wrappers.lambdaUpdate(UserPO.class)
                .set(StrUtil.isNotBlank(cmd.getAccount()), UserPO::getAccount, cmd.getAccount())
                .set(StrUtil.isNotBlank(cmd.getUserName()), UserPO::getUserName, cmd.getUserName())
                .set(StrUtil.isNotBlank(cmd.getPhone()), UserPO::getPhone, cmd.getPhone())
                .set(StrUtil.isNotBlank(cmd.getPassword()), UserPO::getPassword, encodedPassword)
                .set(cmd.getState() != null, UserPO::getState, cmd.getState())
                .eq(UserPO::getId, cmd.getId()));
    }

    public Boolean delete(UserModifyCmd cmd) {
        assertTenantAccess(cmd.getId());
        return userService.removeById(cmd.getId());
    }

    /**
     * 加载当前登录的后台用户。非后台角色（学生/教师等）直接拒绝。
     * 用户管理仅限超管（role=0）与学校管理员（role=1）。
     */
    private UserPO currentUserOrReject() {
        UserPO current = userService.getById(StpUtil.getLoginIdAsInt());
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

    /**
     * 校验当前登录用户对目标 user 记录的租户归属。
     * 通过 DB 读取当前用户的权威角色/schoolId：
     *   * role=0（超管）放行
     *   * 其他角色必须有正数 schoolId，且目标记录 schoolId 必须匹配
     *
     * @param targetUserId 目标用户ID
     */
    private void assertTenantAccess(Integer targetUserId) {
        if (targetUserId == null) {
            return;
        }
        UserPO current = currentUserOrReject();
        if (Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            return;
        }
        Integer currentSchoolId = current.getSchoolId();
        if (currentSchoolId == null || currentSchoolId <= 0) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        UserPO target = userService.getById(targetUserId);
        if (target == null || !Objects.equals(currentSchoolId, target.getSchoolId())) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
    }

    public UserDetailDTO userDetail() {
        UserPO user = userService.getById(StpUtil.getLoginIdAsInt());
        UserDetailDTO userDetailDTO = BeanUtil.copyProperties(user, UserDetailDTO.class);
        if (Objects.nonNull(user) && Objects.nonNull(user.getSchoolId()) && user.getSchoolId() > 0) {
            SchoolPO schoolPO = schoolService.getById(user.getSchoolId());
            if (Objects.nonNull(schoolPO)) {
                userDetailDTO.setSchool(new SchoolSimpleDTO(schoolPO.getId(), schoolPO.getSchoolName()));
                userDetailDTO.setAuthModules(Arrays.asList(schoolPO.getAuthModules().split(",")));
            }
        }
        return userDetailDTO;
    }
}
