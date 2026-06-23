package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.UserPO;

public interface UserService extends IService<UserPO> {
    /**
     * 分页查询用户
     * @param userName 用户名称
     * @param account 用户账户
     * @param state 用户状态
     * @param schoolId 学校ID（租户隔离过滤；传 null 表示不限制，仅超管可用）
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    IPage<UserPO> pageQuery(String userName, String account, Boolean state, Integer schoolId, Integer page, Integer size);

    /**
     * 根据账户查询用户
     * @param account 用户账户
     * @return 用户
     */
    UserPO findByAccount(String account);

    /**
     * 分页查询教师列表
     *
     * @param userName 用户名（模糊搜索）
     * @param account 账号（模糊搜索）
     * @param schoolId 学校ID（可为空）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<UserPO> pageQueryTeacher(String userName, String account, Integer schoolId, Integer page, Integer size);

    /**
     * 根据学校ID统计教师数量
     *
     * @param schoolId 学校ID
     * @return 教师数量
     */
    Long countTeacherBySchoolId(Integer schoolId);
}
