package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.SchoolClassService;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.StudentBackendDTO;
import com.human.digital.digitalhuman.service.model.request.StudentCmd;
import com.human.digital.digitalhuman.service.model.request.StudentQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生管理应用服务（后台）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Service
@RequiredArgsConstructor
public class StudentBackendAppService {

    private final StudentService studentService;
    private final SchoolClassService schoolClassService;
    private final UserService userService;

    /**
     * 分页查询学生
     *
     * @param query 查询条件
     * @return IPage<StudentBackendDTO>
     */
    public IPage<StudentBackendDTO> pageQuery(StudentQuery query) {
        Integer schoolId = getCurrentSchoolId();
        Page<StudentPO> pageParam = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<StudentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getStudentName()), StudentPO::getStudentName, query.getStudentName())
                .like(StringUtils.hasText(query.getIdNumber()), StudentPO::getIdNumber, query.getIdNumber())
                .eq(query.getClassId() != null, StudentPO::getClassId, query.getClassId())
                .eq(query.getSchoolId() != null, StudentPO::getSchoolId, query.getSchoolId())
                .eq(schoolId != null, StudentPO::getSchoolId, schoolId)
                .orderByDesc(StudentPO::getCreateTime);

        IPage<StudentPO> page = studentService.page(pageParam, wrapper);

        // 查询班级信息
        if (page.getRecords().isEmpty()) {
            return page.convert(po -> StudentBackendDTO.of(po, null));
        }

        // 获取班级ID列表
        java.util.Set<Integer> classIds = page.getRecords().stream()
                .map(StudentPO::getClassId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 查询班级信息
        Map<Integer, String> classNameMap = schoolClassService.listByIds(classIds).stream()
                .collect(Collectors.toMap(SchoolClassPO::getId, SchoolClassPO::getClassName));

        return page.convert(po -> StudentBackendDTO.of(po, classNameMap.get(po.getClassId())));
    }

    /**
     * 创建学生
     *
     * @param cmd 命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(StudentCmd cmd) {
        // 检查学号唯一性
        LambdaQueryWrapper<StudentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentPO::getSchoolId, cmd.getSchoolId())
                .eq(StudentPO::getIdNumber, cmd.getIdNumber());
        StudentPO existStudent = studentService.getOne(wrapper);
        if (existStudent != null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NO_DUPLICATE);
        }

        // 创建学生
        StudentPO po = new StudentPO();
        po.setSchoolId(cmd.getSchoolId());
        po.setClassId(cmd.getClassId());
        po.setIdNumber(cmd.getIdNumber());
        po.setStudentName(cmd.getStudentName());
        po.setPassword(cmd.getPassword());
        // 保留schoolClass字段以兼容旧数据
        po.setSchoolClass("");
        studentService.save(po);

        // 更新班级学生数量
        updateClassStudentCount(cmd.getClassId());

        return true;
    }

    /**
     * 修改学生
     *
     * @param cmd 命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(StudentCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }

        StudentPO existPo = studentService.getById(cmd.getId());
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }

        // 检查学号唯一性（排除自己）
        LambdaQueryWrapper<StudentPO> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(StudentPO::getSchoolId, cmd.getSchoolId())
                .eq(StudentPO::getIdNumber, cmd.getIdNumber())
                .ne(StudentPO::getId, cmd.getId());
        StudentPO existStudent = studentService.getOne(checkWrapper);
        if (existStudent != null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NO_DUPLICATE);
        }

        // 记录旧的班级ID
        Integer oldClassId = existPo.getClassId();

        // 更新学生
        existPo.setClassId(cmd.getClassId());
        existPo.setIdNumber(cmd.getIdNumber());
        existPo.setStudentName(cmd.getStudentName());
        // 保留schoolClass字段以兼容旧数据
        existPo.setSchoolClass("");
        if (StringUtils.hasText(cmd.getPassword())) {
            existPo.setPassword(cmd.getPassword());
        }
        studentService.updateById(existPo);

        // 更新班级学生数量
        if (!cmd.getClassId().equals(oldClassId)) {
            updateClassStudentCount(oldClassId);
            updateClassStudentCount(cmd.getClassId());
        }

        return true;
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        StudentPO existPo = studentService.getById(id);
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }

        Integer classId = existPo.getClassId();

        // 删除学生
        studentService.removeById(id);

        // 更新班级学生数量
        updateClassStudentCount(classId);

        return true;
    }

    /**
     * 更新班级学生数量
     *
     * @param classId 班级ID
     */
    private void updateClassStudentCount(Integer classId) {
        if (classId == null) {
            return;
        }
        Long count = studentService.count(new LambdaQueryWrapper<StudentPO>()
                .eq(StudentPO::getClassId, classId));
        SchoolClassPO schoolClass = schoolClassService.getById(classId);
        if (schoolClass != null) {
            schoolClass.setStudentCount(count.intValue());
            schoolClassService.updateById(schoolClass);
        }
    }

    /**
     * 获取当前用户的学校ID
     *
     * @return schoolId
     */
    private Integer getCurrentSchoolId() {
        Integer userId = StpUtil.getLoginIdAsInt();
        UserPO user = userService.getById(userId);
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            return null;
        }
        return user != null ? user.getSchoolId() : null;
    }
}
