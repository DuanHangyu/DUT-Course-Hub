package com.human.digital.digitalhuman.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.StudentMapper;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.service.StudentService;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author taoHouChao
 * @Date 22:12 2025/6/10
 */
@Repository
public class StudentServiceImpl extends ServiceImpl<StudentMapper, StudentPO> implements StudentService {
    @Override
    public IPage<StudentPO> pageQuery(String studentName, String idNumber, String schoolClass, Integer schoolId, Integer page, Integer size) {

        return this.page(new Page<>(page, size), Wrappers.lambdaQuery(StudentPO.class)
                .like(StrUtil.isNotBlank(studentName), StudentPO::getStudentName, studentName)
                .like(StrUtil.isNotBlank(idNumber), StudentPO::getIdNumber, idNumber)
                .like(StrUtil.isNotBlank(schoolClass), StudentPO::getSchoolClass, schoolClass)
                .eq(schoolId != null, StudentPO::getSchoolId, schoolId)
                .orderByDesc(StudentPO::getId));
    }

    @Override
    public List<StudentPO> queryByAccounts(List<String> accounts) {
        return this.list(Wrappers.lambdaQuery(StudentPO.class)
                .in(StudentPO::getIdNumber, accounts));
    }

    @Override
    public Map<Integer, StudentPO> queryByIds(List<Integer> studentIds) {
        if (CollectionUtil.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        return this.list(Wrappers.lambdaQuery(StudentPO.class)
                        .in(StudentPO::getId, studentIds))
                .stream()
                .collect(Collectors.toMap(StudentPO::getId, studentPO -> studentPO));
    }

    @Override
    public List<Integer> queryIdByName(String studentName) {
        if (StrUtil.isBlank(studentName)) {
            return null;
        }
        return this.list(Wrappers.lambdaQuery(StudentPO.class)
                        .select(StudentPO::getId)
                        .like(StudentPO::getStudentName, studentName))
                .stream()
                .map(StudentPO::getId)
                .toList();
    }

    @Override
    public StudentPO findByAccount(String account, Integer schoolId) {
        return this.getOne(Wrappers.lambdaQuery(StudentPO.class)
                .eq(StudentPO::getIdNumber, account)
                .eq(schoolId != null, StudentPO::getSchoolId, schoolId));
    }

    @Override
    public Long countBySchoolId(Integer schoolId) {
        if (schoolId == null) {
            return 0L;
        }
        return this.count(Wrappers.lambdaQuery(StudentPO.class)
                .eq(StudentPO::getSchoolId, schoolId));
    }
}
