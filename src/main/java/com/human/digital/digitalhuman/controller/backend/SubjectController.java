package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.SubjectAppService;
import com.human.digital.digitalhuman.service.model.request.SubjectSortCmd;
import com.human.digital.digitalhuman.service.model.response.SubjectDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学科管理 Controller
 *
 * @Author taoHouChao
 * @Date 10:35 2026/3/10
 */
@RestController
@RequestMapping("/api/backend/subject")
@Tag(name = "学科配置", description = "学科配置管理")
@RequiredArgsConstructor
@Validated
public class SubjectController {

    private final SubjectAppService subjectAppService;

    /**
     * 获取学科列表（含课程数量）
     *
     * @return List<SubjectDTO>
     */
    @GetMapping("/list")
    @Operation(summary = "获取学科列表")
    public List<SubjectDTO> list(@RequestParam("schoolId") Integer schoolId) {
        // 从当前登录用户获取学校ID
        return subjectAppService.listWithCourseCount(schoolId);
    }

    /**
     * 更新学科排序
     *
     * @param cmd 排序命令
     * @return Boolean
     */
    @PutMapping("/sort")
    @Operation(summary = "更新学科排序")
    public Boolean updateSortOrder(@Valid @RequestBody SubjectSortCmd cmd) {
        return subjectAppService.updateSortOrder(cmd.getSchoolId(), cmd);
    }
}
