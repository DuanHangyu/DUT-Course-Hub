package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.PasswordUtils;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.request.LoginDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author taoHouChao
 * @Date 16:27 2025/6/9
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAppService {

    private final UserService userService;

    private final StudentService studentService;

    public String login(LoginDTO loginDTO) {
        boolean isStudent = Objects.equals(Boolean.TRUE, loginDTO.getStudent());
        if (isStudent) {
            StudentPO student = studentService.findByAccount(loginDTO.getAccount(), loginDTO.getSchoolId());
            if (student == null) {
                throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
            }
            if (!PasswordUtils.matches(loginDTO.getPassword(), student.getPassword())) {
                throw new BusinessException(ErrorCodeEnums.USER_PASSWORD_ERROR);
            }
            // 懒迁移：若密码仍是明文，则升级为 BCrypt
            if (!PasswordUtils.isBcryptHash(student.getPassword())) {
                studentService.update(Wrappers.lambdaUpdate(StudentPO.class)
                        .set(StudentPO::getPassword, PasswordUtils.encode(loginDTO.getPassword()))
                        .eq(StudentPO::getId, student.getId()));
                log.info("学生密码已迁移为 BCrypt: studentId={}", student.getId());
            }
            StpUtil.login(student.getId());
            studentService.update(Wrappers.lambdaUpdate(StudentPO.class)
                    .set(StudentPO::getLastLoginTime, LocalDateTime.now())
                    .eq(StudentPO::getId, student.getId()));
            // 学生角色用 3（区别于后台 0/1/2），供拦截器路径级拦截
            StpUtil.getSession().set("role", 3);
            if (student.getSchoolId() != null) {
                StpUtil.getSession().set("schoolId", student.getSchoolId());
            }
        } else {
            UserPO user = userService.findByAccount(loginDTO.getAccount());
            if (user == null) {
                throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
            }
            if (!PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCodeEnums.USER_PASSWORD_ERROR);
            }
            // 懒迁移：若密码仍是明文，则升级为 BCrypt
            if (!PasswordUtils.isBcryptHash(user.getPassword())) {
                userService.update(Wrappers.lambdaUpdate(UserPO.class)
                        .set(UserPO::getPassword, PasswordUtils.encode(loginDTO.getPassword()))
                        .eq(UserPO::getId, user.getId()));
                log.info("用户密码已迁移为 BCrypt: userId={}", user.getId());
            }
            StpUtil.login(user.getId());
            // 保存角色和学校ID到session（供拦截器做路径级权限检查，无需每次查DB）
            StpUtil.getSession().set("role", user.getRole());
            if (user.getSchoolId() != null) {
                StpUtil.getSession().set("schoolId", user.getSchoolId());
            }
        }
        return StpUtil.getTokenInfo().getTokenValue();
    }

    public Boolean logout() {
        StpUtil.logout();
        return true;
    }
}
