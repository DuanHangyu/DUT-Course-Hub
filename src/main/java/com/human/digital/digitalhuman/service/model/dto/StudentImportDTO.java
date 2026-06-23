package com.human.digital.digitalhuman.service.model.dto;

import cn.hutool.core.annotation.Alias;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 学生导入模板 DTO
 *
 * @Author taoHouChao
 * @Date 14:00 2026/3/9
 */
@Data
public class StudentImportDTO {

    @ExcelProperty(value = "学生姓名", index = 0)
    @Alias("学生姓名")
    @ColumnWidth(20)
    private String studentName;

    @ExcelProperty(value = "学号", index = 1)
    @Alias("学号")
    @ColumnWidth(20)
    private String idNumber;

    @ExcelProperty(value = "登录密码", index = 2)
    @Alias("登录密码")
    @ColumnWidth(20)
    private String password;

    @ExcelProperty(value = "所属班级", index = 3)
    @Alias("所属班级")
    @ColumnWidth(20)
    private String className;
}
