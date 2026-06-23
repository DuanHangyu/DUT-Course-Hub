package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.UserAppService;
import com.human.digital.digitalhuman.service.model.request.UserCreateCmd;
import com.human.digital.digitalhuman.service.model.request.UserListQuery;
import com.human.digital.digitalhuman.service.model.request.UserModifyCmd;
import com.human.digital.digitalhuman.service.model.response.UserDetailDTO;
import com.human.digital.digitalhuman.service.model.response.UserSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author taoHouChao
 * @Date 14:28 2025/6/11
 */
@RestController
@RequestMapping("/user")
@Tag(name = "后台用户管理", description = "后台用户管理")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserAppService userAppService;

    @PostMapping("/page-query")
    @Operation(summary = "分页查询")
    public IPage<UserSummaryDTO> pageQuery(@RequestBody UserListQuery query){
        return userAppService.pageQuery(query);
    }

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    public Boolean create(@RequestBody @Validated UserCreateCmd cmd){
        return userAppService.create(cmd);
    }

    @PostMapping("/modify")
    @Operation(summary = "修改用户")
    public Boolean modify(@RequestBody @Validated UserModifyCmd cmd){
        return userAppService.modify(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除用户")
    public Boolean delete(@RequestBody @Validated UserModifyCmd cmd){
        return userAppService.delete(cmd);
    }

    @GetMapping("/detail")
    @Operation(summary = "用户详情")
    public UserDetailDTO userDetail(){
        return userAppService.userDetail();
    }
}
