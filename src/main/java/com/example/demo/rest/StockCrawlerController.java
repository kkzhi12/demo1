package com.example.demo.rest;

import com.example.demo.entity.StockFinanceData;
import com.example.demo.repository.StockFinanceDataRepository;
import com.example.demo.service.StockCacheService;
import com.example.demo.service.StockCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 通用股票财务数据爬取接口
 */
@RestController
@RequestMapping("/api/v1/stock-crawler")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class StockCrawlerController {

    private final StockCrawlerService stockCrawlerService;
    private final StockFinanceDataRepository stockFinanceDataRepository;
    private final StockCacheService stockCacheService;

    /** 要爬取的所有报告期 */
    private static final List<String> ALL_PERIODS = List.of(
            "2023Q1", "2023Q2", "2023Q3", "2023Q4",
            "2024Q1", "2024Q2", "2024Q3", "2024Q4",
            "2025Q1", "2025Q2", "2025Q3", "2025Q4",
            "2026Q1"
    );

    /**
     * 获取指定股票的财务数据
     */
    @RequestMapping(value = "/fetch", method = {RequestMethod.GET, RequestMethod.POST})
    public StockCrawlerService.CrawlResult fetch(
            @RequestParam("stockCode") String stockCode,
            @RequestParam("reportPeriod") String reportPeriod) {
        return stockCrawlerService.crawlStock(stockCode, reportPeriod);
    }

    /**
     * 批量爬取所有缓存中的股票指定季度的财报数据入库
     *
     * @param reportPeriod 报告期，如 2025Q1
     */
    @PostMapping("/crawl-all")
    public Map<String, Object> crawlAll(@RequestParam("reportPeriod") String reportPeriod) {
        List<StockCacheService.StockItem> allStocks = stockCacheService.getAll();
        if (allStocks.isEmpty()) {
            return Map.of("error", "缓存为空，请等待服务启动完成后再试");
        }

        log.info("开始批量爬取所有股票 {} 财报数据，共{}只股票", reportPeriod, allStocks.size());

        // 异步执行，立即返回
        new Thread(() -> doCrawlAll(allStocks, reportPeriod), "crawl-all-thread").start();

        return Map.of(
                "message", "批量爬取任务已启动",
                "stockCount", allStocks.size(),
                "reportPeriod", reportPeriod,
                "estimatedTime", "约" + (allStocks.size() * 600 / 1000 / 60) + "分钟"
        );
    }

    private void doCrawlAll(List<StockCacheService.StockItem> allStocks, String reportPeriod) {
        int totalSuccess = 0;
        int totalFail = 0;
        int totalSkip = 0;

        for (int i = 0; i < allStocks.size(); i++) {
            StockCacheService.StockItem stock = allStocks.get(i);
            String stockCode = stock.getStockCode();

            try {
                // 检查是否已存在，存在就跳过
                if (stockFinanceDataRepository.findByStockCodeAndReportPeriod(stockCode, reportPeriod).isPresent()) {
                    totalSkip++;
                    continue;
                }

                StockCrawlerService.CrawlResult result = stockCrawlerService.crawlStock(stockCode, reportPeriod);
                if (result.isSuccess()) {
                    totalSuccess++;
                } else {
                    totalFail++;
                }
            } catch (Exception e) {
                // 单条失败不影响后续，跳过继续
                totalFail++;
                log.debug("爬取{}失败，跳过: {}", stockCode, e.getMessage());
            }

            // 请求间隔，避免被封
            try { Thread.sleep(600); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if ((i + 1) % 50 == 0) {
                log.info("[{}] 批量爬取进度: {}/{}, 成功:{}, 失败:{}, 跳过:{}",
                        reportPeriod, i + 1, allStocks.size(), totalSuccess, totalFail, totalSkip);
            }
        }

        log.info("[{}] 批量爬取完成！成功={}, 失败={}, 跳过(已存在)={}", reportPeriod, totalSuccess, totalFail, totalSkip);
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
