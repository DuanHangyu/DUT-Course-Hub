package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.enums.UserRoleEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.ExcelUtils;
import com.human.digital.digitalhuman.common.utils.PasswordUtils;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.StudentDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentExportDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentModifyPasswordDTO;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.SchoolStudentDTO;
import com.human.digital.digitalhuman.service.model.response.StudentSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author taoHouChao
 * @Date 21:30 2025/6/10
 */
@Service
@RequiredArgsConstructor
public class StudentAppService {

    private final StudentService studentService;
    private final UserService userService;

    public Boolean create(StudentCreateCmd cmd) {
        StudentPO po = cmd.toPo();
        po.setPassword(PasswordUtils.ensureEncoded(po.getPassword()));
        // 租户隔离：非超管强制 schoolId 为当前管理员所在学校
        UserPO current = currentUserOrReject();
        if (!Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            if (current.getSchoolId() == null || current.getSchoolId() <= 0) {
                throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
            }
            po.setSchoolId(current.getSchoolId());
        }
        try {
            studentService.save(po);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new BusinessException(ErrorCodeEnums.STUDENT_EXIST);
            }
            throw new BusinessException(ErrorCodeEnums.STUDENT_SAVE_ERROR);
        }
        return true;
    }

    public IPage<StudentSummaryDTO> pageQuery(StudentListQuery query) {
        // 学生管理为后台操作：当前登录必须是后台用户（role=0/1）。
        // 从 DB 读取权威角色/schoolId。
        UserPO current = currentUserOrReject();
        Integer schoolId = Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())
                ? null
                : current.getSchoolId();
        if (schoolId == null && !Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        IPage<StudentPO> page = studentService.pageQuery(query.getStudentName(), query.getIdNumber(), query.getSchoolClass(), schoolId, query.getPage(), query.getSize());
        return page.convert(studentPO -> BeanUtil.copyProperties(studentPO, StudentSummaryDTO.class));
    }

    public Boolean modify(StudentModifyCmd cmd) {
        assertTenantAccess(cmd.getId());
        String encodedPassword = StrUtil.isNotBlank(cmd.getPassword())
                ? PasswordUtils.encode(cmd.getPassword())
                : null;
        return studentService.update(Wrappers.lambdaUpdate(StudentPO.class)
                .set(StrUtil.isNotBlank(cmd.getIdNumber()), StudentPO::getIdNumber, cmd.getIdNumber())
                .set(StrUtil.isNotBlank(cmd.getStudentName()), StudentPO::getStudentName, cmd.getStudentName())
                .set(StrUtil.isNotBlank(cmd.getSchoolClass()), StudentPO::getSchoolClass, cmd.getSchoolClass())
                .set(StrUtil.isNotBlank(cmd.getPassword()), StudentPO::getPassword, encodedPassword)
                .eq(StudentPO::getId, cmd.getId()));
    }

    public Boolean delete(StudentModifyCmd cmd) {
        assertTenantAccess(cmd.getId());
        return studentService.removeById(cmd.getId());
    }

    /**
     * 校验当前登录用户对目标 student 记录的租户归属。
     * 学生管理为后台操作：当前登录必须是后台用户（role=0/1）。
     *
     * @param targetStudentId 目标学生ID
     */
    private void assertTenantAccess(Integer targetStudentId) {
        if (targetStudentId == null) {
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
        StudentPO target = studentService.getById(targetStudentId);
        if (target == null || !Objects.equals(currentSchoolId, target.getSchoolId())) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
    }

    /**
     * 加载当前登录的后台用户。学生 token 或非后台角色（role != 0/1）直接拒绝。
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

    /**
     * 解析当前用户的租户作用域：超管返回 null（跨租户），学校管理员必须返回有效 schoolId。
     * 非超管若 schoolId 缺失则 fail-closed，避免租户过滤被静默跳过。
     */
    private Integer resolveSchoolScopeOrReject(UserPO current) {
        if (Integer.valueOf(UserRoleEnums.SUPER_ADMIN.getCode()).equals(current.getRole())) {
            return null;
        }
        Integer schoolId = current.getSchoolId();
        if (schoolId == null || schoolId <= 0) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
        return schoolId;
    }

    public void exportStudent(StudentExportCmd cmd) {
        // 租户隔离：限定只能导出本校学生
        UserPO current = currentUserOrReject();
        Integer schoolId = resolveSchoolScopeOrReject(current);
        List<StudentPO> list = studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                .in(StudentPO::getId, cmd.getIds())
                .eq(schoolId != null, StudentPO::getSchoolId, schoolId));
        List<StudentExportDTO> exportDTOS = list.stream().map(StudentExportDTO::new).toList();
        try {
            ExcelUtils.exportExcel(exportDTOS, StudentExportDTO.COLUMN_WIDTH);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_EXPORT_ERROR);
        }
    }

    public void exportTemplate() {
        List<StudentExportDTO> exportDTOS = new ArrayList<>();
        exportDTOS.add(new StudentExportDTO());
        try {
            ExcelUtils.exportExcel(exportDTOS, StudentExportDTO.COLUMN_WIDTH);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_EXPORT_ERROR);
        }
    }

    public void importStudent(List<StudentImportCmd> students) {
        // 租户隔离：非超管强制写入当前 schoolId；schoolId 缺失则 fail-closed
        UserPO current = currentUserOrReject();
        Integer forcedSchoolId = resolveSchoolScopeOrReject(current);
        List<StudentPO> studentPos = students.stream().map(StudentImportCmd::toPo).toList();
        // 写入前对明文密码做 BCrypt 哈希；非超管强制 schoolId
        studentPos.forEach(po -> {
            po.setPassword(PasswordUtils.ensureEncoded(po.getPassword()));
            if (forcedSchoolId != null) {
                po.setSchoolId(forcedSchoolId);
            }
        });
        // 过滤已存在的学生（直接过滤将要保存的 studentPos，避免过滤源与保存源不一致）
        List<String> accounts = studentPos.stream().map(StudentPO::getIdNumber).distinct().toList();
        List<StudentPO> existStudents = studentService.queryByAccounts(accounts);
        Set<String> existsAccounts = existStudents.stream().map(StudentPO::getIdNumber).collect(Collectors.toSet());
        List<StudentPO> toSave = studentPos.stream()
                .filter(po -> !existsAccounts.contains(po.getIdNumber()))
                .toList();
        // 保存
        if (!toSave.isEmpty()) {
            studentService.saveBatch(toSave);
        }
    }

    public StudentDTO info() {
        int studentId = StpUtil.getLoginIdAsInt();
        StudentPO student = checkAndGetStudentPO(studentId);
        return BeanUtil.copyProperties(student, StudentDTO.class);
    }

    public List<String> allSchoolClass() {
        // 租户隔离：非超管只返回本校班级
        UserPO current = currentUserOrReject();
        Integer schoolId = resolveSchoolScopeOrReject(current);
        return studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                        .eq(schoolId != null, StudentPO::getSchoolId, schoolId))
                .stream().map(StudentPO::getSchoolClass).filter(Objects::nonNull).distinct().toList();
    }

    public Boolean modifyPassword(StudentModifyPasswordDTO modifyPasswordDTO) {
        int studentId = StpUtil.getLoginIdAsInt();
        StudentPO studentPO = checkAndGetStudentPO(studentId);
        if (PasswordUtils.matches(modifyPasswordDTO.getOriginalPassword(), studentPO.getPassword())) {
            studentPO.setPassword(PasswordUtils.encode(modifyPasswordDTO.getNewPassword()));
            return studentService.updateById(studentPO);
        }
        throw new BusinessException(ErrorCodeEnums.USER_PASSWORD_ERROR);
    }

    private StudentPO checkAndGetStudentPO(Integer account) {
        StudentPO studentPO = studentService.getById(account);
        if (studentPO == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }
        return studentPO;
    }

    public List<SchoolStudentDTO> schoolStudent() {
        // 租户隔离：非超管只返回本校学生
        UserPO current = currentUserOrReject();
        Integer schoolId = resolveSchoolScopeOrReject(current);
        List<StudentPO> students = studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                .eq(schoolId != null, StudentPO::getSchoolId, schoolId));
        Map<String, List<StudentSummaryDTO>> schoolNameMap = students.stream()
                .filter(s -> s.getSchoolClass() != null)
                .collect(Collectors.groupingBy(StudentPO::getSchoolClass, Collectors.mapping(item -> BeanUtil.copyProperties(item, StudentSummaryDTO.class), Collectors.toList())));
        List<SchoolStudentDTO> result = new ArrayList<>();
        schoolNameMap.forEach((key, value) -> {
            SchoolStudentDTO schoolStudentDTO = new SchoolStudentDTO();
            schoolStudentDTO.setSchoolClass(key);
            schoolStudentDTO.setStudents(value);
            result.add(schoolStudentDTO);
        });
        return result;
    }
}
