package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.HomeBannerAppService;
import com.human.digital.digitalhuman.service.HomeBackgroundAppService;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.HomeBannerDTO;
import com.human.digital.digitalhuman.service.model.response.HomeBackgroundDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页配置后台管理接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@RestController
@RequestMapping("/home")
@Tag(name = "首页配置管理", description = "首页 Banner 和底图配置管理")
@Validated
public class HomeController {

    @Resource
    private HomeBannerAppService homeBannerAppService;

    @Resource
    private HomeBackgroundAppService homeBackgroundAppService;

    // ==================== Banner 接口 ====================

    @PostMapping("/banner/page-query")
    @Operation(summary = "分页查询 Banner")
    public IPage<HomeBannerDTO> bannerPageQuery(@RequestBody HomeBannerQuery query) {
        return homeBannerAppService.pageQuery(query);
    }

    @PostMapping("/banner/create")
    @Operation(summary = "新增 Banner")
    public Boolean bannerCreate(@RequestBody @Valid HomeBannerCmd cmd) {
        return homeBannerAppService.create(cmd);
    }

    @PostMapping("/banner/modify")
    @Operation(summary = "修改 Banner")
    public Boolean bannerModify(@RequestBody @Valid HomeBannerCmd cmd) {
        return homeBannerAppService.modify(cmd);
    }

    @PostMapping("/banner/delete")
    @Operation(summary = "删除 Banner")
    public Boolean bannerDelete(@RequestParam("id") Integer id) {
        return homeBannerAppService.delete(id);
    }

    @PostMapping("/banner/move")
    @Operation(summary = "上移/下移 Banner")
    public Boolean bannerMove(@RequestBody @Valid HomeBannerMoveCmd cmd) {
        return homeBannerAppService.move(cmd);
    }

    // ==================== 底图接口 ====================

    @PostMapping("/background/page-query")
    @Operation(summary = "分页查询底图")
    public IPage<HomeBackgroundDTO> backgroundPageQuery(@RequestBody HomeBackgroundQuery query) {
        return homeBackgroundAppService.pageQuery(query);
    }

    @PostMapping("/background/create")
    @Operation(summary = "新增底图")
    public Boolean backgroundCreate(@RequestBody @Valid HomeBackgroundCmd cmd) {
        return homeBackgroundAppService.create(cmd);
    }

    @PostMapping("/background/modify")
    @Operation(summary = "修改底图")
    public Boolean backgroundModify(@RequestBody @Valid HomeBackgroundCmd cmd) {
        return homeBackgroundAppService.modify(cmd);
    }

    @PostMapping("/background/delete")
    @Operation(summary = "删除底图")
    public Boolean backgroundDelete(@RequestParam("id") Integer id) {
        return homeBackgroundAppService.delete(id);
    }

    @PostMapping("/background/move")
    @Operation(summary = "上移/下移底图")
    public Boolean backgroundMove(@RequestBody @Valid HomeBackgroundMoveCmd cmd) {
        return homeBackgroundAppService.move(cmd);
    }
}
