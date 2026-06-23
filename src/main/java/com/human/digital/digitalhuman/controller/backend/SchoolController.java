package com.human.digital.digitalhuman.controller.backend;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.SchoolAppService;
import com.human.digital.digitalhuman.service.model.dto.SchoolDTO;
import com.human.digital.digitalhuman.service.model.request.SchoolCmd;
import com.human.digital.digitalhuman.service.model.request.SchoolQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学校管理后台接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@RestController
@RequestMapping("/school")
@Tag(name = "学校管理", description = "高中学校信息管理")
@Validated
public class SchoolController {

    @Resource
    private SchoolAppService schoolAppService;

    @PostMapping("/page-query")
    @Operation(summary = "分页查询学校")
    public IPage<SchoolDTO> pageQuery(@RequestBody SchoolQuery query) {
        return schoolAppService.pageQuery(query);
    }

    @PostMapping("/create")
    @Operation(summary = "新增学校")
    public Boolean create(@RequestBody @Valid SchoolCmd cmd) {
        return schoolAppService.create(cmd);
    }

    @PostMapping("/modify")
    @Operation(summary = "修改学校")
    public Boolean modify(@RequestBody @Valid SchoolCmd cmd) {
        return schoolAppService.modify(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除学校")
    public Boolean delete(@RequestParam("id") Integer id) {
        return schoolAppService.delete(id);
    }
}
