package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.StudentBackendAppService;
import com.human.digital.digitalhuman.service.model.dto.StudentBackendDTO;
import com.human.digital.digitalhuman.service.model.request.StudentCmd;
import com.human.digital.digitalhuman.service.model.request.StudentQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学生管理后台接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@RestController
@RequestMapping("/api/backend/student")
@Tag(name = "学生管理", description = "学生信息管理（后台）")
@Validated
@Deprecated
public class StudentBackendController {

    @Resource
    private StudentBackendAppService studentBackendAppService;

    @GetMapping("/list")
    @Operation(summary = "学生列表")
    public IPage<StudentBackendDTO> list(StudentQuery query) {
        return studentBackendAppService.pageQuery(query);
    }

    @PostMapping("/create")
    @Operation(summary = "新增学生")
    public Boolean create(@RequestBody @Valid StudentCmd cmd) {
        return studentBackendAppService.create(cmd);
    }

    @PutMapping("/update")
    @Operation(summary = "编辑学生")
    public Boolean update(@RequestBody @Valid StudentCmd cmd) {
        return studentBackendAppService.update(cmd);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除学生")
    public Boolean delete(@PathVariable("id") Integer id) {
        return studentBackendAppService.delete(id);
    }
}
