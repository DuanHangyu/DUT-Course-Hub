package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.utils.ExcelUtils;
import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import com.human.digital.digitalhuman.repository.po.SchoolPO;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.SchoolClassService;
import com.human.digital.digitalhuman.repository.service.SchoolService;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.StudentImportDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentImportResultDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentBackendDTO;
import com.human.digital.digitalhuman.service.model.request.StudentCmd;
import com.human.digital.digitalhuman.service.model.request.StudentQuery;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生管理服务（简化版）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Service
@RequiredArgsConstructor
public class SchoolStudentAppService {

    private final StudentService studentService;
    private final SchoolClassService schoolClassService;
    private final SchoolService schoolService;
    private final UserService userService;

    /**
     * 分页查询学生
     *
     * @param query 查询条件
     * @return 分页结果
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
     * 获取学生详情
     *
     * @param id 学生ID
     * @return 学生信息
     */
    public StudentBackendDTO getById(Integer id) {
        StudentPO po = studentService.getById(id);
        if (po == null) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }

        String className = null;
        if (po.getClassId() != null) {
            SchoolClassPO schoolClass = schoolClassService.getById(po.getClassId());
            className = schoolClass != null ? schoolClass.getClassName() : null;
        }
        return StudentBackendDTO.of(po, className);
    }

    /**
     * 创建学生
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(StudentCmd cmd) {
        // 检查学校学生数量上限
        checkStudentLimit(cmd.getSchoolId(), 1);

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
        po.setSchoolClass("");
        studentService.save(po);

        // 更新班级学生数量
        updateClassStudentCount(cmd.getClassId());

        return true;
    }

    /**
     * 更新学生
     *
     * @param cmd 更新命令
     * @return 是否成功
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
        existPo.setSchoolClass("");
        if (StringUtils.hasText(cmd.getPassword())) {
            existPo.setPassword(cmd.getPassword());
        }
        studentService.updateById(existPo);

        // 更新班级学生数量
        if (!Objects.equals(cmd.getClassId(), oldClassId)) {
            updateClassStudentCount(oldClassId);
            updateClassStudentCount(cmd.getClassId());
        }

        return true;
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 是否成功
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
     * 检查学校学生数量是否已达上限
     *
     * @param schoolId    学校ID
     * @param addCount    新增学生数量
     */
    private void checkStudentLimit(Integer schoolId, int addCount) {
        if (schoolId == null) {
            return;
        }
        SchoolPO school = schoolService.getById(schoolId);
        if (school == null || school.getMaxStudents() == null) {
            return;
        }
        Long currentCount = studentService.countBySchoolId(schoolId);
        if (currentCount + addCount > school.getMaxStudents()) {
            throw new BusinessException(ErrorCodeEnums.SCHOOL_STUDENT_LIMIT);
        }
    }

    /**
     * 获取当前用户的学校ID
     *
     * @return 学校ID
     */
    private Integer getCurrentSchoolId() {
        Integer userId = StpUtil.getLoginIdAsInt();
        UserPO user = userService.getById(userId);
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            return null;
        }
        return user != null ? user.getSchoolId() : null;
    }

    /**
     * 导出学生导入模板
     *
     * @param schoolId 学校ID
     * @throws IOException IO异常
     */
    public void exportTemplate(Integer schoolId) throws IOException {
        // 查询班级列表
        LambdaQueryWrapper<SchoolClassPO> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(SchoolClassPO::getSchoolId, schoolId);
        List<SchoolClassPO> classList = schoolClassService.list(classWrapper);

        // 构建下拉数据 Map<String, List<String>>
        Map<String, List<String>> dropdownData = new HashMap<>();
        dropdownData.put("className", classList.stream()
                .map(SchoolClassPO::getClassName)
                .collect(Collectors.toList()));

        // 导出模板
        ExcelUtils.exportTemplateWithDropdown(StudentImportDTO.class, dropdownData, "学生导入模板.xlsx");
    }

    /**
     * 导入学生（遇错停止策略）
     * 任何一条数据校验失败时，立即抛出异常触发事务回滚
     *
     * @param schoolId 学校ID
     * @param file     Excel文件
     * @return 导入结果
     * @throws IOException IO异常
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentImportResultDTO importStudents(Integer schoolId, MultipartFile file) throws IOException {
        // 解析Excel
        List<StudentImportDTO> importList = ExcelUtils.importExcel(file.getInputStream(), StudentImportDTO.class);
        if (importList == null || importList.isEmpty()) {
            return StudentImportResultDTO.fail("导入文件为空");
        }

        // 查询班级列表，构建 className -> classId 映射
        LambdaQueryWrapper<SchoolClassPO> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(SchoolClassPO::getSchoolId, schoolId);
        List<SchoolClassPO> classList = schoolClassService.list(classWrapper);
        Map<String, Integer> classNameToIdMap = classList.stream()
                .collect(Collectors.toMap(SchoolClassPO::getClassName, SchoolClassPO::getId, (a, b) -> a));

        // 查询已存在的学号
        Set<String> existIdNumbers = studentService.list(new LambdaQueryWrapper<StudentPO>()
                        .eq(StudentPO::getSchoolId, schoolId))
                .stream()
                .map(StudentPO::getIdNumber)
                .collect(Collectors.toSet());

        // 校验并构建学生列表（遇错停止）
        List<StudentPO> toSave = new ArrayList<>();

        for (int i = 0; i < importList.size(); i++) {
            StudentImportDTO dto = importList.get(i);
            int rowNum = i + 1;

            // 校验学号 - 为空则抛异常
            if (!StringUtils.hasText(dto.getIdNumber())) {
                throw new BusinessException(ErrorCodeEnums.STUDENT_NO_DUPLICATE.getCode(),
                        "第" + rowNum + "行：学号不能为空");
            }

            // 校验学号重复 - 存在则抛异常
            if (existIdNumbers.contains(dto.getIdNumber())) {
                throw new BusinessException(ErrorCodeEnums.STUDENT_NO_DUPLICATE.getCode(),
                        "第" + rowNum + "行：学号[" + dto.getIdNumber() + "]已存在");
            }

            // 班级匹配 - 找不到则抛异常
            if (!StringUtils.hasText(dto.getClassName())) {
                throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST.getCode(),
                        "第" + rowNum + "行：班级不能为空");
            }
            Integer classId = classNameToIdMap.get(dto.getClassName());
            if (classId == null) {
                throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST.getCode(),
                        "第" + rowNum + "行：班级[" + dto.getClassName() + "]不存在");
            }

            // 构建学生实体
            StudentPO po = new StudentPO();
            po.setSchoolId(schoolId);
            po.setClassId(classId);
            po.setIdNumber(dto.getIdNumber());
            po.setStudentName(dto.getStudentName());
            po.setPassword(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456");
            po.setSchoolClass("");
            toSave.add(po);
            existIdNumbers.add(dto.getIdNumber());
        }

        // 检查学校学生数量上限
        checkStudentLimit(schoolId, toSave.size());

        // 批量保存学生
        if (!toSave.isEmpty()) {
            studentService.saveBatch(toSave);

            // 更新班级学生数量
            Set<Integer> classIds = toSave.stream()
                    .map(StudentPO::getClassId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            for (Integer classId : classIds) {
                updateClassStudentCount(classId);
            }
        }

        // 返回成功结果
        return StudentImportResultDTO.success(toSave.size());
    }

    /**
     * 条件导出学生
     *
     * @param query 查询条件
     * @throws IOException IO异常
     */
    public void exportStudents(StudentQuery query) throws IOException {
        // 设置固定导出数量
        query.setSize(10000);

        // 查询学生数据
        List<StudentPO> studentList;
        List<Integer> ids = query.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            studentList = studentService.listByIds(ids);
        }else {
            LambdaQueryWrapper<StudentPO> wrapper = buildQueryWrapper(query);
            studentList = studentService.list(wrapper);
        }

        if (studentList.isEmpty()) {
            throw new BusinessException(ErrorCodeEnums.STUDENT_NOT_EXIST);
        }

        // 查询班级信息，构建 classId -> className 映射
        Set<Integer> classIds = studentList.stream()
                .map(StudentPO::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        final Map<Integer, String> classIdToNameMap;
        if (!classIds.isEmpty()) {
            classIdToNameMap = schoolClassService.listByIds(classIds).stream()
                    .collect(Collectors.toMap(SchoolClassPO::getId, SchoolClassPO::getClassName, (a, b) -> a));
        } else {
            classIdToNameMap = new HashMap<>();
        }

        // 转换为 StudentImportDTO 列表
        List<StudentImportDTO> exportList = studentList.stream()
                .map(po -> {
                    StudentImportDTO dto = new StudentImportDTO();
                    dto.setStudentName(po.getStudentName());
                    dto.setIdNumber(po.getIdNumber());
                    dto.setPassword(po.getPassword());
                    dto.setClassName(classIdToNameMap.get(po.getClassId()));
                    return dto;
                })
                .collect(Collectors.toList());

        // 列宽配置
        Map<Integer, Integer> columnWidth = new HashMap<>();
        columnWidth.put(0, 20);
        columnWidth.put(1, 20);
        columnWidth.put(2, 20);
        columnWidth.put(3, 20);

        // 导出Excel
        ExcelUtils.exportExcel(exportList, columnWidth);
    }

    /**
     * 构建查询条件
     *
     * @param query 查询条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<StudentPO> buildQueryWrapper(StudentQuery query) {
        Integer schoolId = getCurrentSchoolId();
        LambdaQueryWrapper<StudentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getStudentName()), StudentPO::getStudentName, query.getStudentName())
                .like(StringUtils.hasText(query.getIdNumber()), StudentPO::getIdNumber, query.getIdNumber())
                .eq(query.getClassId() != null, StudentPO::getClassId, query.getClassId())
                .eq(query.getSchoolId() != null, StudentPO::getSchoolId, query.getSchoolId())
                .eq(schoolId != null, StudentPO::getSchoolId, schoolId)
                .orderByDesc(StudentPO::getCreateTime);
        return wrapper;
    }
}
