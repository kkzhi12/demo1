package com.example.demo.entity;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : wangyuzhi
 * @date: 2025/6/5  11:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockInfo {

    @ExcelProperty("股票名称")
    private String stockName;

    @ExcelProperty("股票编号")
    private String stockNumber;

    @ExcelProperty("股票价格")
    private Double stockPrice;

    @ExcelProperty("股票分红")
    private Double stockDividend;

    /**
     * 分红率
     */
    private Double stockDividendRate;

    /**
     * 平均roe
     */
    private Double stockRoeAvg;

    /**
     * 归一化后的股票分红率
     */
    private Double normalizedStockDividend;

    /**
     * 归一化后的股票roe
     */
    private Double normalizedStockRoe;

    /**
     * 得分
     */
    private Double score;

}
