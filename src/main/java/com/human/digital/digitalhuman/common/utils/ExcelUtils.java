package com.human.digital.digitalhuman.common.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author taoHouChao
 * @Date 10:48 2025/6/11
 */
public class ExcelUtils {

    public static <T> void exportExcel(List<T> data, Map<Integer, Integer> columnWidth) throws IOException {
        HttpServletResponse response = getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("学生信息", "UTF-8") + ".xlsx");

        // 创建ExcelWriter
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 只导出设置了别名的字段
        writer.setOnlyAlias(true);

        // 写入数据
        writer.write(data, true);
        // 设置列宽
        for (Map.Entry<Integer, Integer> entry : columnWidth.entrySet()) {
            writer.setColumnWidth(entry.getKey(), entry.getValue());
        }

        // 输出到浏览器
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);

        // 关闭writer
        writer.close();
        IoUtil.close(out);
    }

    /**
     * 导入 Excel
     *
     * @param inputStream 输入流
     * @param clazz       目标类
     * @return 导入的数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        return reader.readAll(clazz);
    }

    /**
     * 导出带下拉列表的模板
     *
     * @param clazz         目标类
     * @param dropdownData  下拉数据，key 为字段名，value 为下拉选项列表
     * @param fileName      文件名
     * @throws IOException IO 异常
     */
    public static void exportTemplateWithDropdown(Class<?> clazz, Map<String, List<String>> dropdownData, String fileName) throws IOException {
        HttpServletResponse response = getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 使用 Hutool 创建 ExcelWriter
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 只处理设置了别名的字段
        writer.setOnlyAlias(true);

        // 获取底层 Workbook
        Workbook workbook = writer.getWorkbook();

        // 获取字段和列名
        List<String> headers = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                headers.add(annotation.value()[0]);
                fieldNames.add(field.getName());
            }
        }

        // 写入表头
        for (int i = 0; i < headers.size(); i++) {
            writer.writeCellValue(i, 0, headers.get(i));
        }

        // 设置下拉列表
        Sheet sheet = workbook.getSheetAt(0);
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        for (Map.Entry<String, List<String>> entry : dropdownData.entrySet()) {
            int colIndex = fieldNames.indexOf(entry.getKey());
            if (colIndex >= 0 && entry.getValue() != null && !entry.getValue().isEmpty()) {
                List<String> data = entry.getValue();

                // 直接使用单元格数据验证（更简单的方式）
                // 将下拉数据用逗号分隔
                String[] dropdownArray = data.toArray(new String[0]);

                // 设置数据验证：起始行1，结束行1000，起始列colIndex，结束列colIndex
                CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, colIndex, colIndex);
                DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(dropdownArray);
                DataValidation validation = validationHelper.createValidation(constraint, addressList);
                validation.setShowErrorBox(true);
                workbook.getSheetAt(0).addValidationData(validation);
            }
        }

        // 输出到浏览器
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);

        // 关闭writer
        writer.close();
        IoUtil.close(out);
    }


    private static HttpServletResponse getResponse(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCodeEnums.GET_RESPONSE_ERROR);
        }
        return attributes.getResponse();
    }
}
