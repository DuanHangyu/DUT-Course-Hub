package com.human.digital.digitalhuman.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.HomeBannerPO;
import com.human.digital.digitalhuman.repository.service.HomeBannerService;
import com.human.digital.digitalhuman.service.model.request.HomeBannerCmd;
import com.human.digital.digitalhuman.service.model.request.HomeBannerMoveCmd;
import com.human.digital.digitalhuman.service.model.request.HomeBannerQuery;
import com.human.digital.digitalhuman.service.model.response.HomeBannerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页 Banner 应用服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Service
@RequiredArgsConstructor
public class HomeBannerAppService {

    private final HomeBannerService homeBannerService;

    private final OssClientUtils ossClientUtils;

    /**
     * 分页查询 Banner
     *
     * @param query 查询条件
     * @return IPage<HomeBannerDTO>
     */
    public IPage<HomeBannerDTO> pageQuery(HomeBannerQuery query) {
        return pageQuery(query, true);
    }

    /**
     * 分页查询 Banner
     *
     * @param query       查询条件
     * @param signedUrl   是否生成签名URL
     * @return IPage<HomeBannerDTO>
     */
    public IPage<HomeBannerDTO> pageQuery(HomeBannerQuery query, boolean signedUrl) {
        IPage<HomeBannerPO> page = homeBannerService.pageQuery(
                query.getPage(),
                query.getSize(),
                query.getName()
        );
        if (signedUrl) {
            return page.convert(po -> HomeBannerDTO.of(po, ossClientUtils::getSignedUrl));
        }
        return page.convert(HomeBannerDTO::of);
    }

    /**
     * 新增 Banner
     *
     * @param cmd 请求命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(HomeBannerCmd cmd) {
        HomeBannerPO po = cmd.toPo();
        // 获取当前最大 sort 值
        Integer maxSort = homeBannerService.getMaxSort();
        po.setSort(maxSort == null ? 0 : maxSort + 1);
        return homeBannerService.save(po);
    }

    /**
     * 修改 Banner
     *
     * @param cmd 请求命令
     * @return Boolean
     */
    public Boolean modify(HomeBannerCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException(400, "ID不能为空");
        }
        HomeBannerPO existPo = homeBannerService.getById(cmd.getId());
        if (existPo == null) {
            throw new BusinessException(500, "Banner 不存在");
        }
        existPo.setName(cmd.getName());
        existPo.setImageUrl(cmd.getImageUrl());
        return homeBannerService.updateById(existPo);
    }

    /**
     * 删除 Banner
     *
     * @param id Banner ID
     * @return Boolean
     */
    public Boolean delete(Integer id) {
        return homeBannerService.removeById(id);
    }

    /**
     * 上移/下移 Banner
     *
     * @param cmd 移动命令
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean move(HomeBannerMoveCmd cmd) {
        HomeBannerPO currentPo = homeBannerService.getById(cmd.getId());
        if (currentPo == null) {
            throw new BusinessException(500, "Banner 不存在");
        }

        // 获取所有 Banner 按 sort 排序
        List<HomeBannerPO> allList = homeBannerService.list(Wrappers.lambdaQuery(HomeBannerPO.class)
                .orderByAsc(HomeBannerPO::getSort)
                .orderByAsc(HomeBannerPO::getId));

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
        HomeBannerPO targetPo = allList.get(targetIndex);
        Integer currentSort = currentPo.getSort();
        Integer targetSort = targetPo.getSort();

        currentPo.setSort(targetSort);
        targetPo.setSort(currentSort);

        homeBannerService.updateById(currentPo);
        homeBannerService.updateById(targetPo);

        return true;
    }

    /**
     * 获取 Banner 列表（前端接口）
     *
     * @return List<HomeBannerDTO>
     */
    public List<HomeBannerDTO> list() {
        List<HomeBannerPO> list = homeBannerService.listOrderBySort();
        return list.stream()
                .map(po -> HomeBannerDTO.of(po, ossClientUtils::getSignedUrl))
                .collect(Collectors.toList());
    }
}
