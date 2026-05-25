package com.example.demo.rest;

import com.example.demo.service.EastMoneyCrawlerService;
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
     * 获取支持的银行股列表
     */
    @GetMapping("/supported-banks")
    public Map<String, String> supportedBanks() {
        return crawlerService.getSupportedBanks();
    }
}
