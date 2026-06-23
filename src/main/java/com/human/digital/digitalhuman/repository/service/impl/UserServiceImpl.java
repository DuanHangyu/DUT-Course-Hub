package com.human.digital.digitalhuman.repository.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.UserMapper;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.UserService;
import org.springframework.stereotype.Repository;

/**
 * @Author taoHouChao
 * @Date 14:27 2025/6/11
 */
@Repository
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {
    @Override
    public IPage<UserPO> pageQuery(String userName, String account, Boolean state, Integer schoolId, Integer page, Integer size) {
        return this.page(new Page<>(page, size), Wrappers.lambdaQuery(UserPO.class)
                .like(StrUtil.isNotBlank(userName),  UserPO::getUserName, userName)
                .like(StrUtil.isNotBlank(account),  UserPO::getAccount, account)
                .eq(state != null, UserPO::getState, state)
                .eq(schoolId != null, UserPO::getSchoolId, schoolId)
                .orderByDesc(UserPO::getId));
    }

    @Override
    public UserPO findByAccount(String account) {
        return this.getOne(Wrappers.lambdaQuery(UserPO.class)
                .eq(UserPO::getAccount, account));
    }

    @Override
    public IPage<UserPO> pageQueryTeacher(String userName, String account, Integer schoolId, Integer page, Integer size) {
        IPage<UserPO> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<UserPO> wrapper = Wrappers.lambdaQuery(UserPO.class)
                .eq(UserPO::getRole, 2) // 教师角色
                .like(StrUtil.isNotBlank(userName), UserPO::getUserName, userName)
                .like(StrUtil.isNotBlank(account), UserPO::getAccount, account)
                .eq(schoolId != null, UserPO::getSchoolId, schoolId)
                .orderByDesc(UserPO::getCreateTime);

        return this.page(pageParam, wrapper);
    }

    @Override
    public Long countTeacherBySchoolId(Integer schoolId) {
        if (schoolId == null) {
            return 0L;
        }
        return this.count(Wrappers.lambdaQuery(UserPO.class)
                .eq(UserPO::getRole, 2) // 教师角色
                .eq(UserPO::getSchoolId, schoolId));
    }
}
