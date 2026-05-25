package com.example.demo.entity;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : wangyuzhi
 * @date: 2025/6/5  15:23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRoe {

    @ExcelProperty("股票编号")
    private String stockNumber;

    @ExcelProperty("股票Roe")
    private String stockRoeComma;

}
