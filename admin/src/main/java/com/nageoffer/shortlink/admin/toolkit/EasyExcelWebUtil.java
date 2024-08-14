package com.nageoffer.shortlink.admin.toolkit;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 封装 EasyExcel 操作 Web 工具方法
 */
public class EasyExcelWebUtil {

    /**
     * 通过 HTTP 响应输出 Excel 文件.
     *
     * @param response HttpServletResponse 对象，用于输出 Excel 文件到客户端.
     * @param fileName 输出的 Excel 文件名称，不需要包含扩展名 ".xlsx" (它会自动添加).
     * @param clazz Excel 中的数据对应的实体类类型，每一行数据将映射为该类的一个实例.
     * @param data 要写入 Excel 文件的数据列表，列表中的每个对象对应 Excel 中的一行.
     */
    @SneakyThrows
    public static void write(HttpServletResponse response, String fileName, Class<?> clazz, List<?> data) {
        // 1. 设置响应的内容类型为 Excel 文件的 MIME 类型.
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //2. 将文件名进行 URL 编码，确保文件名在不同浏览器中都能正确显示.
        response.setCharacterEncoding("utf-8");
        //将文件名进行 URL 编码，使用 UTF-8 编码格式，同时替换编码过程中产生的加号（`+`）为 `%20`，以符合浏览器解析规范。
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        // 设置响应头，告知浏览器这是一个附件下载，并且文件名带有 `.xlsx` 扩展名.
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        // 使用 EasyExcel 库将数据写入响应的输出流，并生成 Excel 文件.
        EasyExcel.write(response.getOutputStream(), clazz).sheet("Sheet").doWrite(data);
    }

}

