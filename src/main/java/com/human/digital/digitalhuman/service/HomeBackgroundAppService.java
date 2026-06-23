package com.human.digital.digitalhuman.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.HomeBackgroundPO;
import com.human.digital.digitalhuman.repository.service.HomeBackgroundService;
import com.human.digital.digitalhuman.service.model.request.HomeBackgroundCmd;
import com.human.digital.digitalhuman.service.model.request.HomeBackgroundMoveCmd;
import com.human.digital.digitalhuman.service.model.request.HomeBackgroundQuery;
import com.human.digital.digitalhuman.service.model.response.HomeBackgroundDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页底图应用服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Service
@RequiredArgsConstructor
public class HomeBackgroundAppService {

    private final HomeBackgroundService homeBackgroundService;

    private final OssClientUtils ossClientUtils;

    /**
     * 分页查询底图
     *
     * @param query 查询条件
     * @return IPage<HomeBackgroundDTO>
     */
    public IPage<HomeBackgroundDTO> pageQuery(HomeBackgroundQuery query) {
        return pageQuery(query, true);
    }

    /**
     * 分页查询底图
     *
     * @param query       查询条件
     * @param signedUrl   是否生成签名URL
     * @return IPage<HomeBackgroundDTO>
     */
    public IPage<HomeBackgroundDTO> pageQuery(HomeBackgroundQuery query, boolean signedUrl) {
        IPage<HomeBackgroundPO> page = homeBackgroundService.pageQuery(
                query.getPage(),
                query.getSize(),
                query.getName()
        );
        if (signedUrl) {
            return page.convert(po -> HomeBackgroundDTO.of(po, ossClientUtils::getSignedUrl));
        }
        return page.convert(HomeBackgroundDTO::of);
    }

    /**
     * 新增底图
     *
     * @param cmd 请求命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(HomeBackgroundCmd cmd) {
        HomeBackgroundPO po = cmd.toPo();
        // 获取当前最大 sort 值
        Integer maxSort = homeBackgroundService.getMaxSort();
        po.setSort(maxSort == null ? 0 : maxSort + 1);
        return homeBackgroundService.save(po);
    }

    /**
     * 修改底图
     *
     * @param cmd 请求命令
     * @return Boolean
     */
    public Boolean modify(HomeBackgroundCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException(400, "ID不能为空");
        }
        HomeBackgroundPO existPo = homeBackgroundService.getById(cmd.getId());
        if (existPo == null) {
            throw new BusinessException(500, "底图不存在");
        }
        existPo.setName(cmd.getName());
        existPo.setImageUrl(cmd.getImageUrl());
        existPo.setJumpUrl(cmd.getJumpUrl());
        return homeBackgroundService.updateById(existPo);
    }

    /**
     * 删除底图
     *
     * @param id 底图 ID
     * @return Boolean
     */
    public Boolean delete(Integer id) {
        return homeBackgroundService.removeById(id);
    }

    /**
     * 上移/下移底图
     *
     * @param cmd 移动命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean move(HomeBackgroundMoveCmd cmd) {
        HomeBackgroundPO currentPo = homeBackgroundService.getById(cmd.getId());
        if (currentPo == null) {
            throw new BusinessException(500, "底图不存在");
        }

        // 获取所有底图按 sort 排序
        List<HomeBackgroundPO> allList = homeBackgroundService.list(Wrappers.lambdaQuery(HomeBackgroundPO.class)
                .orderByAsc(HomeBackgroundPO::getSort)
                .orderByAsc(HomeBackgroundPO::getId));

        if (CollectionUtil.isEmpty(allList) || allList.size() == 1) {
            return true;
        }

        int currentIndex = -1;
        for (int i = 0; i < allList.size(); i++) {
            if (allList.get(i).getId().equals(cmd.getId())) {
                currentIndex = i;
                break;
            }
        }

        int targetIndex = -1;
        if ("up".equals(cmd.getDirection())) {
            targetIndex = currentIndex - 1;
        } else if ("down".equals(cmd.getDirection())) {
            targetIndex = currentIndex + 1;
        }

        if (targetIndex < 0 || targetIndex >= allList.size()) {
            return true; // 边界情况，不移动
        }

        // 交换 sort 值
        HomeBackgroundPO targetPo = allList.get(targetIndex);
        Integer currentSort = currentPo.getSort();
        Integer targetSort = targetPo.getSort();

        currentPo.setSort(targetSort);
        targetPo.setSort(currentSort);

        homeBackgroundService.updateById(currentPo);
        homeBackgroundService.updateById(targetPo);

        return true;
    }

    /**
     * 获取底图列表（前端接口）
     *
     * @return List<HomeBackgroundDTO>
     */
    public List<HomeBackgroundDTO> list() {
        List<HomeBackgroundPO> list = homeBackgroundService.listOrderBySort();
        return list.stream()
                .map(po -> HomeBackgroundDTO.of(po, ossClientUtils::getSignedUrl))
                .collect(Collectors.toList());
    }
}
