package com.example.demo.rest;

import com.example.demo.entity.BankStockData;
import com.example.demo.service.BankStockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 银行股数据管理接口
 * 提供Excel导入和数据查询功能
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/bank-stock-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BankStockDataController {

    private final BankStockDataService bankStockDataService;

    /**
     * Excel导入银行股数据
     *
     * @param file         Excel文件
     * @param reportPeriod 报告期（如 2024Q4、2025Q1）
     * @param dataSource   数据来源（可选，默认"Excel导入"）
     */
    @PostMapping("/import")
    public BankStockDataService.ImportResult importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("reportPeriod") String reportPeriod,
            @RequestParam(value = "dataSource", defaultValue = "Excel导入") String dataSource) {
        return bankStockDataService.importFromExcel(file, reportPeriod, dataSource);
    }

    /**
     * 查询指定报告期的所有银行数据
     */
    @GetMapping("/list")
    public List<BankStockData> listByPeriod(@RequestParam("reportPeriod") String reportPeriod) {
        return bankStockDataService.findByReportPeriod(reportPeriod);
    }

    /**
     * 查询某只银行股的历史数据
     */
    @GetMapping("/history/{stockCode}")
    public List<BankStockData> history(@PathVariable String stockCode) {
        return bankStockDataService.findByStockCode(stockCode);
    }

    /**
     * 查询所有可用的报告期列表
     */
    @GetMapping("/periods")
    public List<String> periods() {
        return bankStockDataService.findAllReportPeriods();
    }

    /**
     * 查询所有数据
     */
    @GetMapping("/all")
    public List<BankStockData> all() {
        return bankStockDataService.findAll();
    }
}
