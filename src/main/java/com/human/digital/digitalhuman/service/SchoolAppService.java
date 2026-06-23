package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.common.enums.UserRoleEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.utils.PasswordUtils;
import com.human.digital.digitalhuman.repository.po.SchoolPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.SchoolService;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.SchoolDTO;
import com.human.digital.digitalhuman.service.model.request.SchoolCmd;
import com.human.digital.digitalhuman.service.model.request.SchoolQuery;
import com.human.digital.digitalhuman.service.model.response.SchoolSimpleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学校应用服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Service
@RequiredArgsConstructor
public class SchoolAppService {

    private final SchoolService schoolService;
    private final UserService userService;
    private final StudentService studentService;

    /**
     * 分页查询学校
     *
     * @param query 查询条件
     * @return IPage<SchoolDTO>
     */
    public IPage<SchoolDTO> pageQuery(SchoolQuery query) {
        requireSuperAdmin();
        IPage<SchoolPO> page = schoolService.pageQuery(
                query.getPage(),
                query.getSize(),
                query.getSchoolName()
        );
        return page.convert(po -> {
            // 查询管理员信息
            UserPO adminUser = null;
            if (po.getAdminUserId() != null) {
                adminUser = userService.getById(po.getAdminUserId());
            }
            String adminName = adminUser != null ? adminUser.getUserName() : null;
            String adminAccount = adminUser != null ? adminUser.getAccount() : null;
            // 不向前端返回密码哈希

            // 查询学生和教师数量
            Long studentCount = studentService.countBySchoolId(po.getId());
            Long teacherCount = userService.countTeacherBySchoolId(po.getId());

            return SchoolDTO.of(po, adminName, adminAccount, null,
                    studentCount.intValue(), teacherCount.intValue());
        });
    }

    /**
     * 新增学校
     *
     * @param cmd 请求命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(SchoolCmd cmd) {
        requireSuperAdmin();
        // 检查学校名称唯一性
        SchoolPO existSchool = schoolService.getBySchoolName(cmd.getSchoolName());
        if (existSchool != null) {
            throw new BusinessException(ErrorCodeEnums.SCHOOL_NAME_DUPLICATE);
        }

        // 检查账号唯一性
        UserPO existUser = userService.findByAccount(cmd.getAdminAccount());
        if (existUser != null) {
            throw new BusinessException(ErrorCodeEnums.ACCOUNT_EXIST);
        }

        // 验证 IP 白名单格式
        if (cmd.getAccessPolicy() == 2 && StrUtil.isNotBlank(cmd.getIpWhitelist())) {
            String[] ips = cmd.getIpWhitelist().split(",");
            for (String ip : ips) {
                String trimmedIp = ip.trim();
                if (!isValidIp(trimmedIp)) {
                    throw new BusinessException(ErrorCodeEnums.INVALID_IP_WHITELIST);
                }
            }
        }

        // 创建管理员账号
        UserPO adminUser = new UserPO();
        adminUser.setUserName(cmd.getAdminName());
        adminUser.setAccount(cmd.getAdminAccount());
        adminUser.setPassword(PasswordUtils.ensureEncoded(cmd.getAdminPassword()));
        adminUser.setState(true);
        adminUser.setRole(UserRoleEnums.SCHOOL_ADMIN.getCode()); // 2 = 学校管理员
        userService.save(adminUser);

        // 创建学校记录
        SchoolPO schoolPO = cmd.toPo();
        schoolPO.setAdminUserId(adminUser.getId());
        schoolService.save(schoolPO);

        adminUser.setSchoolId(schoolPO.getId());
        userService.updateById(adminUser);

        return true;
    }

    /**
     * 修改学校
     *
     * @param cmd 请求命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean modify(SchoolCmd cmd) {
        requireSuperAdmin();
        if (cmd.getId() == null) {
            throw new BusinessException(ErrorCodeEnums.SCHOOL_NOT_EXIST);
        }

        SchoolPO existPo = schoolService.getById(cmd.getId());
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.SCHOOL_NOT_EXIST);
        }

        // 检查学校名称唯一性（排除自己）
        SchoolPO existSchool = schoolService.getBySchoolName(cmd.getSchoolName());
        if (existSchool != null && !existSchool.getId().equals(cmd.getId())) {
            throw new BusinessException(ErrorCodeEnums.SCHOOL_NAME_DUPLICATE);
        }

        // 检查账号唯一性（排除自己）
        UserPO existUser = userService.findByAccount(cmd.getAdminAccount());
        if (existUser != null && !existUser.getId().equals(existPo.getAdminUserId())) {
            throw new BusinessException(ErrorCodeEnums.ACCOUNT_EXIST);
        }

        // 验证 IP 白名单格式
        if (cmd.getAccessPolicy() == 2 && StrUtil.isNotBlank(cmd.getIpWhitelist())) {
            String[] ips = cmd.getIpWhitelist().split(",");
            for (String ip : ips) {
                String trimmedIp = ip.trim();
                if (!isValidIp(trimmedIp)) {
                    throw new BusinessException(ErrorCodeEnums.INVALID_IP_WHITELIST);
                }
            }
        }

        // 更新管理员账号
        if (existPo.getAdminUserId() != null) {
            UserPO adminUser = userService.getById(existPo.getAdminUserId());
            if (adminUser != null) {
                adminUser.setUserName(cmd.getAdminName());
                adminUser.setAccount(cmd.getAdminAccount());
                if (StrUtil.isNotBlank(cmd.getAdminPassword())) {
                    adminUser.setPassword(PasswordUtils.encode(cmd.getAdminPassword()));
                }
                userService.updateById(adminUser);
            }
        }

        // 更新学校记录
        existPo.setSchoolName(cmd.getSchoolName());
        existPo.setPrincipalName(cmd.getPrincipalName());
        existPo.setPhone(cmd.getPhone());
        existPo.setLocation(cmd.getLocation());
        existPo.setState(cmd.getState());
        existPo.setDomain(cmd.getDomain());
        existPo.setAccessPolicy(cmd.getAccessPolicy());
        existPo.setIpWhitelist(cmd.getIpWhitelist());
        existPo.setLogoUrl(cmd.getLogoUrl());
        if (cmd.getAuthModules() != null && !cmd.getAuthModules().isEmpty()) {
            existPo.setAuthModules(String.join(",", cmd.getAuthModules()));
        }
        existPo.setMaxStudents(cmd.getMaxStudents());
        existPo.setAiTokens(cmd.getAiTokens());
        schoolService.updateById(existPo);

        return true;
    }

    /**
     * 删除学校
     *
     * @param id 学校ID
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        requireSuperAdmin();
        SchoolPO existPo = schoolService.getById(id);
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.SCHOOL_NOT_EXIST);
        }

        // 删除管理员账号
        if (existPo.getAdminUserId() != null) {
            userService.removeById(existPo.getAdminUserId());
        }

        // 删除学校
        return schoolService.removeById(id);
    }

    /**
     * 校验 IP 格式
     *
     * @param ip IP 地址
     * @return boolean
     */
    private boolean isValidIp(String ip) {
        // 简单的 IP 格式校验，支持 IPv4
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipv4Pattern);
    }

    public List<SchoolSimpleDTO> allSchool() {
        List<SchoolPO> schoolPOS = schoolService.list();
        return schoolPOS.stream()
                .map(item -> new SchoolSimpleDTO(item.getId(), item.getSchoolName()))
                .toList();
    }

    /**
     * 校验当前登录用户为超级管理员（role=0）。学校管理仅限超管，防止学校管理员/教师越权。
     */
    private void requireSuperAdmin() {
        Integer currentUserId = StpUtil.getLoginIdAsInt();
        UserPO currentUser = userService.getById(currentUserId);
        if (currentUser == null || !Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(currentUser.getRole())) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
    }
}
