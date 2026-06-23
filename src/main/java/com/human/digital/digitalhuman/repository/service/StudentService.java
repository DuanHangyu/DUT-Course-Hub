package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudentPO;

import java.util.List;
import java.util.Map;

public interface StudentService extends IService<StudentPO> {
    /**
     * 分页查询学生信息
     * @param studentName 学生名称
     * @param idNumber 学号
     * @param schoolClass 班级
     * @param schoolId 学校ID（租户隔离过滤；传 null 表示不限制，仅超管可用）
     * @param page 页码
     * @param size 每页数量
     * @return IPage<StudentPO>
     */
    IPage<StudentPO> pageQuery(String studentName, String idNumber, String schoolClass, Integer schoolId, Integer page, Integer size);

    /**
     * 根据账户查询学生信息
     * @param accounts 账户
     * @return List<StudentPO>
     */
    List<StudentPO> queryByAccounts(List<String> accounts);

    /**
     * 根据id查询学生信息
     * @param studentIds id
     * @return Map<Integer, StudentPO>
     */
    Map<Integer, StudentPO> queryByIds(List<Integer> studentIds);

    /**
     * 根据名称查询id
     * @param studentName 名称
     * @return List<Integer>
     */
    List<Integer> queryIdByName(String studentName);

    /**
     * 根据账户查询学生信息
     *
     * @param account  账户
     * @param schoolId 学校id
     * @return StudentPO
     */
    StudentPO findByAccount(String account, Integer schoolId);

    /**
     * 根据学校ID统计学生数量
     *
     * @param schoolId 学校ID
     * @return 学生数量
     */
    Long countBySchoolId(Integer schoolId);
}
