package com.human.digital.digitalhuman.controller.backend;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.service.StudentAppService;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.SchoolStudentDTO;
import com.human.digital.digitalhuman.service.model.response.StudentSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Author taoHouChao
 * @Date 21:26 2025/6/10
 */
@RestController
@RequestMapping("/backend/student")
@Tag(name = "后台学生管理", description = "后台学生管理")
@RequiredArgsConstructor
@Validated
@Deprecated
public class StudentController {

    private final StudentAppService studentAppService;

    @PostMapping("/page-query")
    @Operation(summary = "分页查询")
    public IPage<StudentSummaryDTO> pageQuery(@RequestBody StudentListQuery query){
        return studentAppService.pageQuery(query);
    }

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    public Boolean create(@RequestBody @Validated StudentCreateCmd cmd){
        return studentAppService.create(cmd);
    }

    @PostMapping("/modify")
    @Operation(summary = "修改学生")
    public Boolean modify(@RequestBody StudentModifyCmd cmd){
        return studentAppService.modify(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除学生")
    public Boolean delete(@RequestBody StudentModifyCmd cmd){
        return studentAppService.delete(cmd);
    }

    @PostMapping("/export")
    @Operation(summary = "导出学生")
    public void exportStudent(@RequestBody StudentExportCmd cmd){
        studentAppService.exportStudent(cmd);
    }

    @PostMapping("/export-template")
    @Operation(summary = "导出学生模板")
    public void exportTemplate(){
        studentAppService.exportTemplate();
    }

    @PostMapping("/import")
    @Operation(summary = "导入学生")
    public void importStudent(@RequestBody MultipartFile file) throws IOException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<StudentImportCmd> students = reader.readAll(StudentImportCmd.class);
        studentAppService.importStudent(students);
    }

    @GetMapping("/all-school-class")
    @Operation(summary = "获取所有班级")
    public List<String> allSchoolClass(){
        return studentAppService.allSchoolClass();
    }

    @GetMapping("/all-student")
    @Operation(summary = "获取所有学生")
    public List<StudentSummaryDTO> allStudent(){
        StudentListQuery query = new StudentListQuery();
        query.setPage(1);
        query.setSize(10000);
        return studentAppService.pageQuery(query).getRecords();
    }

    @GetMapping("/school-student")
    @Operation(summary = "获取所有学生")
    public List<SchoolStudentDTO> schoolStudent(){
        return studentAppService.schoolStudent();
    }
}
