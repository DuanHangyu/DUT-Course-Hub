package com.human.digital.digitalhuman.controller.api;

import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.StudentAppService;
import com.human.digital.digitalhuman.service.model.dto.StudentDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentModifyPasswordDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @USER taoHouChao
 * @DATE 21:57 2025/6/16
 */
@RestController
@RequestMapping("/api/student")
@Tag(name = "前台学生管理", description = "前台学生管理")
public class StudentApiController {

    @Resource
    private StudentAppService studentAppService;

    @GetMapping("/info")
    @Operation(summary = "获取学生信息")
    public StudentDTO info(){
        return studentAppService.info();
    }

    @PostMapping("/modify-password")
    @Operation(summary = "修改学生密码")
    @OperateLog("修改学生密码")
    public Boolean modifyPassword(@RequestBody StudentModifyPasswordDTO modifyPasswordDTO){
        return studentAppService.modifyPassword(modifyPasswordDTO);
    }
}
