package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.TeacherAppService;
import com.human.digital.digitalhuman.service.model.request.TeacherCreateCmd;
import com.human.digital.digitalhuman.service.model.request.TeacherListQuery;
import com.human.digital.digitalhuman.service.model.request.TeacherModifyCmd;
import com.human.digital.digitalhuman.service.model.response.TeacherSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 教师管理控制器
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@RestController
@RequestMapping("/api/teacher")
@Tag(name = "教师管理", description = "教师管理")
@RequiredArgsConstructor
@Validated
@Deprecated
public class TeacherController {

    private final TeacherAppService teacherAppService;

    @PostMapping("/page-query")
    @Operation(summary = "分页查询教师列表")
    public IPage<TeacherSummaryDTO> pageQuery(@RequestBody TeacherListQuery query) {
        return teacherAppService.pageQuery(query);
    }

    @PostMapping
    @Operation(summary = "新增教师")
    public Boolean create(@RequestBody @Valid TeacherCreateCmd cmd) {
        return teacherAppService.create(cmd);
    }

    @PutMapping
    @Operation(summary = "编辑教师")
    public Boolean modify(@RequestBody @Valid TeacherModifyCmd cmd) {
        return teacherAppService.modify(cmd);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除教师")
    public Boolean delete(@PathVariable("id") Integer id) {
        return teacherAppService.delete(id);
    }
}
