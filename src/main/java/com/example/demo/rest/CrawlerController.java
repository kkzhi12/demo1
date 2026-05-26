package com.example.demo.rest;

import com.example.demo.service.EastMoneyCrawlerService;
import com.example.demo.service.StockCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据爬取接口
 * 从东方财富爬取银行股财务数据
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CrawlerController {

    private final EastMoneyCrawlerService crawlerService;
    private final StockCacheService stockCacheService;

    /**
     * 爬取单只银行股数据
     *
     * @param stockCode    股票代码（如 601398）
     * @param reportPeriod 报告期（如 2024Q4）
     */
    @RequestMapping(value = "/single", method = {RequestMethod.GET, RequestMethod.POST})
    public EastMoneyCrawlerService.CrawlResult crawlSingle(
            @RequestParam("stockCode") String stockCode,
            @RequestParam("reportPeriod") String reportPeriod) {
        return crawlerService.crawlSingleStock(stockCode, reportPeriod);
    }

    /**
     * 批量爬取所有银行股数据
     *
     * @param reportPeriod 报告期（如 2024Q4）
     */
    @RequestMapping(value = "/all-banks", method = {RequestMethod.GET, RequestMethod.POST})
    public List<EastMoneyCrawlerService.CrawlResult> crawlAllBanks(
            @RequestParam("reportPeriod") String reportPeriod) {
        return crawlerService.crawlAllBanks(reportPeriod);
    }

    /**
     * 获取支持的银行股列表（静态配置）
     */
    @GetMapping("/supported-banks")
    public Map<String, String> supportedBanks() {
        return crawlerService.getSupportedBanks();
    }

    /**
     * 从东方财富接口动态获取所有A股股票代码列表
     * 注意：数据量较大（约5000只），请求耗时较长
     */
    @GetMapping("/fetch-all-stocks")
    public Map<String, String> fetchAllStocks() {
        return crawlerService.fetchAllStockCodes();
    }

    /**
     * 从东方财富接口动态获取银行板块股票列表
     * 返回最新的银行股代码和名称
     */
    @GetMapping("/fetch-bank-stocks")
    public Map<String, String> fetchBankStocks() {
        return crawlerService.fetchBankStockCodes();
    }

    /**
     * 使用动态银行股列表批量爬取财报数据
     * 会先从东方财富获取最新银行板块成分股，再逐一爬取
     *
     * @param reportPeriod 报告期（如 2024Q4）
     */
    @RequestMapping(value = "/all-banks-dynamic", method = {RequestMethod.GET, RequestMethod.POST})
    public List<EastMoneyCrawlerService.CrawlResult> crawlAllBanksDynamic(
            @RequestParam("reportPeriod") String reportPeriod) {
        return crawlerService.crawlAllBanksDynamic(reportPeriod);
    }

    /**
     * 搜索股票（从本地缓存搜索，支持代码或名称模糊匹配）
     * 缓存在服务启动时从东方财富加载
     *
     * @param keyword 搜索关键字
     * @param limit   最大返回条数，默认30
     */
    @GetMapping("/search-stock")
    public List<StockCacheService.StockItem> searchStock(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "limit", defaultValue = "30") int limit) {
        return stockCacheService.search(keyword, limit);
    }

    /**
     * 获取缓存状态
     */
    @GetMapping("/cache-status")
    public Map<String, Object> cacheStatus() {
        return Map.of("cacheSize", stockCacheService.getCacheSize());
    }

    /**
     * 手动触发：从东方财富拉取所有A股代码并入库
     * 只需成功调用一次，后续启动会自动从数据库加载
     */
    @PostMapping("/load-all-stocks")
    public Map<String, Object> loadAllStocks() {
        int count = stockCacheService.fetchAndSave();
        if (count > 0) {
            return Map.of("success", true, "message", "成功加载并入库", "count", count);
        } else {
            return Map.of("success", false, "message", "加载失败，可能被限流，请稍后再试");
        }
    }
}
