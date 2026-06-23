package com.human.digital.digitalhuman.controller.backend;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.service.model.dto.CustomerServiceDTO;
import com.human.digital.digitalhuman.service.CustomerServiceAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 客服智能体接口
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@RestController
@RequestMapping("/backend/customer-service")
@RequiredArgsConstructor
@Validated
@Tag(name = "客服智能体", description = "智能客服对话接口")
public class CustomerServiceController {

    private final CustomerServiceAppService customerServiceAppService;

    @PostMapping("/chat")
    @Operation(summary = "对话接口（异步，WebSocket接收结果）")
    public CustomerServiceDTO chat(@RequestBody CustomerServiceDTO dto) {
        dto.setUserId(StpUtil.getLoginIdAsInt());
        return customerServiceAppService.submitChat(dto);
    }

    @GetMapping("/history")
    @Operation(summary = "获取对话历史")
    public CustomerServiceDTO getHistory(@RequestParam("sessionId") String sessionId) {
        return customerServiceAppService.getHistory(sessionId);
    }
}
