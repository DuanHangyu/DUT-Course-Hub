package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.ClassAppService;
import com.human.digital.digitalhuman.service.model.dto.ClassDTO;
import com.human.digital.digitalhuman.service.model.request.ClassCmd;
import com.human.digital.digitalhuman.service.model.request.ClassQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 班级管理后台接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@RestController
@RequestMapping("/api/backend/class")
@Tag(name = "班级管理", description = "班级架构管理")
@Validated
@Deprecated
public class ClassController {

    @Resource
    private ClassAppService classAppService;

    @GetMapping("/list")
    @Operation(summary = "班级列表")
    public IPage<ClassDTO> list(ClassQuery query) {
        return classAppService.pageQuery(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "班级详情")
    public ClassDTO detail(@PathVariable("id") Integer id) {
        return classAppService.getById(id);
    }

    @PostMapping("/create")
    @Operation(summary = "新增班级")
    public Boolean create(@RequestBody @Valid ClassCmd cmd) {
        return classAppService.create(cmd);
    }

    @PutMapping("/update")
    @Operation(summary = "编辑班级")
    public Boolean update(@RequestBody @Valid ClassCmd cmd) {
        return classAppService.update(cmd);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除班级")
    public Boolean delete(@PathVariable("id") Integer id) {
        return classAppService.delete(id);
    }
}
