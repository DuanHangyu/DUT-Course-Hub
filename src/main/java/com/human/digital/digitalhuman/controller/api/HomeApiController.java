package com.human.digital.digitalhuman.controller.api;

import com.human.digital.digitalhuman.service.HomeBannerAppService;
import com.human.digital.digitalhuman.service.HomeBackgroundAppService;
import com.human.digital.digitalhuman.service.model.response.HomeBannerDTO;
import com.human.digital.digitalhuman.service.model.response.HomeBackgroundDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页配置前端接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@RestController
@RequestMapping("/api/home")
@Tag(name = "首页配置", description = "首页 Banner 和底图前端接口")
public class HomeApiController {

    @Resource
    private HomeBannerAppService homeBannerAppService;

    @Resource
    private HomeBackgroundAppService homeBackgroundAppService;

    @GetMapping("/banner/list")
    @Operation(summary = "获取 Banner 列表")
    public List<HomeBannerDTO> bannerList() {
        return homeBannerAppService.list();
    }

    @GetMapping("/background/list")
    @Operation(summary = "获取底图列表")
    public List<HomeBackgroundDTO> backgroundList() {
        return homeBackgroundAppService.list();
    }
}
