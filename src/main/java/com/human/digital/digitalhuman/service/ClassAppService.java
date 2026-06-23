package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.SchoolClassService;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.ClassDTO;
import com.human.digital.digitalhuman.service.model.request.ClassCmd;
import com.human.digital.digitalhuman.service.model.request.ClassQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 班级应用服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Service
@RequiredArgsConstructor
public class ClassAppService {

    private final SchoolClassService schoolClassService;
    private final UserService userService;
    private final StudentService studentService;

    /**
     * 分页查询班级
     *
     * @param query 查询条件
     * @return IPage<ClassDTO>
     */
    public IPage<ClassDTO> pageQuery(ClassQuery query) {
        // 如果查询参数有schoolId，优先使用查询参数；否则使用当前用户的学校ID
        Integer schoolId = query.getSchoolId() != null ? query.getSchoolId() : getCurrentSchoolId();
        IPage<SchoolClassPO> page = schoolClassService.pageQuery(
                query.getPage(),
                query.getSize(),
                schoolId,
                query.getClassName()
        );
        return page.convert(po -> {
            String teacherName = null;
            if (po.getTeacherId() != null) {
                UserPO teacher = userService.getById(po.getTeacherId());
                teacherName = teacher != null ? teacher.getUserName() : null;
            }
            return ClassDTO.of(po, teacherName);
        });
    }

    /**
     * 获取班级详情
     *
     * @param id 班级ID
     * @return ClassDTO
     */
    public ClassDTO getById(Integer id) {
        SchoolClassPO po = schoolClassService.getById(id);
        if (po == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }
        // 权限校验
        checkPermission(po.getSchoolId());

        String teacherName = null;
        if (po.getTeacherId() != null) {
            UserPO teacher = userService.getById(po.getTeacherId());
            teacherName = teacher != null ? teacher.getUserName() : null;
        }
        return ClassDTO.of(po, teacherName);
    }

    /**
     * 创建班级
     *
     * @param cmd 命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(ClassCmd cmd) {
        // 权限校验
        checkPermission(cmd.getSchoolId());

        // 检查班级名称唯一性
        SchoolClassPO existClass = schoolClassService.getBySchoolIdAndClassName(cmd.getSchoolId(), cmd.getClassName());
        if (existClass != null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NAME_DUPLICATE);
        }

        // 创建班级
        SchoolClassPO po = new SchoolClassPO();
        po.setSchoolId(cmd.getSchoolId());
        po.setClassName(cmd.getClassName());
        po.setTeacherId(cmd.getTeacherId());
        po.setStudentCount(0);
        schoolClassService.save(po);

        return true;
    }

    /**
     * 修改班级
     *
     * @param cmd 命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ClassCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }

        SchoolClassPO existPo = schoolClassService.getById(cmd.getId());
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }

        // 权限校验
        checkPermission(existPo.getSchoolId());

        // 检查班级名称唯一性（排除自己）
        SchoolClassPO existClass = schoolClassService.getBySchoolIdAndClassName(existPo.getSchoolId(), cmd.getClassName());
        if (existClass != null && !existClass.getId().equals(cmd.getId())) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NAME_DUPLICATE);
        }

        // 更新班级
        existPo.setClassName(cmd.getClassName());
        existPo.setTeacherId(cmd.getTeacherId());
        schoolClassService.updateById(existPo);

        return true;
    }

    /**
     * 删除班级（级联删除学生）
     *
     * @param id 班级ID
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        SchoolClassPO existPo = schoolClassService.getById(id);
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }

        // 权限校验
        checkPermission(existPo.getSchoolId());

        // 删除该班级下的所有学生
        studentService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.human.digital.digitalhuman.repository.po.StudentPO>()
                .eq(com.human.digital.digitalhuman.repository.po.StudentPO::getClassId, id));

        // 删除班级
        schoolClassService.removeById(id);

        return true;
    }

    /**
     * 获取当前用户的学校ID
     *
     * @return schoolId
     */
    private Integer getCurrentSchoolId() {
        Integer userId = StpUtil.getLoginIdAsInt();
        UserPO user = userService.getById(userId);
        // 超级管理员可以看到所有学校
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            return null;
        }
        return user != null ? user.getSchoolId() : null;
    }

    /**
     * 权限校验
     *
     * @param targetSchoolId 目标学校ID
     */
    private void checkPermission(Integer targetSchoolId) {
        Integer userId = StpUtil.getLoginIdAsInt();
        UserPO user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_EXIST);
        }
        // 超级管理员可以操作所有学校
        if (user.getRole() != null && user.getRole() == 0) {
            return;
        }
        // 学校管理员只能操作本校
        if (!user.getSchoolId().equals(targetSchoolId)) {
            throw new BusinessException(ErrorCodeEnums.NO_PERMISSION);
        }
    }
}
