package com.example.demo.rest;

import com.example.demo.entity.StockFinanceData;
import com.example.demo.repository.StockFinanceDataRepository;
import com.example.demo.service.StockCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通用股票财务数据爬取接口
 */
@RestController
@RequestMapping("/api/v1/stock-crawler")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockCrawlerController {

    private final StockCrawlerService stockCrawlerService;
    private final StockFinanceDataRepository stockFinanceDataRepository;

    /**
     * 获取指定股票的财务数据
     *
     * @param stockCode    股票代码（如 601398、000001、300750）
     * @param reportPeriod 报告期（如 2024Q4）
     */
    @RequestMapping(value = "/fetch", method = {RequestMethod.GET, RequestMethod.POST})
    public StockCrawlerService.CrawlResult fetch(
            @RequestParam("stockCode") String stockCode,
            @RequestParam("reportPeriod") String reportPeriod) {
        return stockCrawlerService.crawlStock(stockCode, reportPeriod);
    }

    /**
     * 按报告期查询已保存的数据列表
     */
    @GetMapping("/list")
    public List<StockFinanceData> list(@RequestParam("reportPeriod") String reportPeriod) {
        return stockFinanceDataRepository.findByReportPeriod(reportPeriod);
    }

    /**
     * 查询指定股票的历史数据
     */
    @GetMapping("/history/{stockCode}")
    public List<StockFinanceData> history(@PathVariable String stockCode) {
        return stockFinanceDataRepository.findByStockCodeOrderByReportPeriodDesc(stockCode);
    }

    /**
     * 获取所有已保存的报告期列表
     */
    @GetMapping("/periods")
    public List<String> periods() {
        return stockFinanceDataRepository.findAllReportPeriods();
    }
}
