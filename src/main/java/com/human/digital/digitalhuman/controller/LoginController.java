package com.human.digital.digitalhuman.controller;

import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.LoginAppService;
import com.human.digital.digitalhuman.service.SchoolAppService;
import com.human.digital.digitalhuman.service.model.request.LoginDTO;
import com.human.digital.digitalhuman.service.model.response.SchoolSimpleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 15:26 2025/6/9
 */
@RestController
@RequestMapping
@Tag(name = "登录接口", description = "登录接口")
@Validated
public class LoginController {

    @Resource
    private LoginAppService loginAppService;

    @Resource
    private SchoolAppService schoolAppService;

    @GetMapping("/allSchool")
    @Operation(summary = "查询所有学校")
    public List<SchoolSimpleDTO> allSchool() {
        return schoolAppService.allSchool();
    }

    @PostMapping("/login")
    @Operation(summary = "登录接口")
    @OperateLog(value = "登录")
    public String login(@RequestBody @Valid LoginDTO loginDTO) {
        return loginAppService.login(loginDTO);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出接口")
    @OperateLog(value = "登出")
    public Boolean logout(){
        return loginAppService.logout();
    }
}
