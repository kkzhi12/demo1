package com.example.demo.service;

import com.example.demo.entity.StockFinanceData;
import com.example.demo.repository.StockFinanceDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 通用股票财务数据爬虫服务
 * 通过东方财富公开接口获取任意股票的财务指标数据
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockCrawlerService {

    private final StockFinanceDataRepository stockFinanceDataRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 爬取指定股票的财务指标数据
     *
     * @param stockCode    股票代码（如 601398、000001）
     * @param reportPeriod 报告期（如 2024Q4）
     * @return 爬取结果
     */
    @Transactional
    public CrawlResult crawlStock(String stockCode, String reportPeriod) {
        try {
            String marketSuffix = stockCode.startsWith("6") ? "SH" : "SZ";
            String secuCode = stockCode + "." + marketSuffix;

            String apiUrl = "https://datacenter.eastmoney.com/securities/api/data/v1/get?"
                    + "reportName=RPT_F10_FINANCE_MAINFINADATA"
                    + "&columns=ALL"
                    + "&filter=(SECUCODE%3D%22" + secuCode + "%22)"
                    + "&pageNumber=1&pageSize=20"
                    + "&sortTypes=-1&sortColumns=REPORT_DATE";

            log.info("请求URL: {}", apiUrl);
            String json = doGet(apiUrl);

            if (json == null || json.isBlank()) {
                return CrawlResult.fail(stockCode, "未知", "接口返回为空");
            }

            JsonNode root = objectMapper.readTree(json);
            if (!root.has("result") || root.get("result").isNull()) {
                return CrawlResult.fail(stockCode, "未知", "接口返回无数据");
            }

            JsonNode result = root.get("result");
            JsonNode dataArray = result.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
                return CrawlResult.fail(stockCode, "未知", "未找到该股票数据");
            }

            // Match report period
            String targetDate = convertReportPeriodToDate(reportPeriod);
            JsonNode matched = null;
            for (JsonNode item : dataArray) {
                String reportDate = item.has("REPORT_DATE") ? item.get("REPORT_DATE").asText() : "";
                if (reportDate.startsWith(targetDate)) {
                    matched = item;
                    break;
                }
            }

            if (matched == null) {
                String name = dataArray.get(0).has("SECURITY_NAME_ABBR") ? dataArray.get(0).get("SECURITY_NAME_ABBR").asText() : "未知";
                return CrawlResult.fail(stockCode, name, "该报告期（" + reportPeriod + "）数据尚未发布");
            }

            String stockName = matched.has("SECURITY_NAME_ABBR") ? matched.get("SECURITY_NAME_ABBR").asText() : "未知";

            StockFinanceData data = new StockFinanceData();
            data.setStockCode(stockCode);
            data.setStockName(stockName);
            data.setReportPeriod(reportPeriod);
            data.setReportType(getText(matched, "REPORT_TYPE"));
            data.setImportTime(LocalDateTime.now());
            data.setDataSource("东方财富爬取");

            // 每股指标
            data.setEpsJb(getDecimal(matched, "EPSJB"));
            data.setEpsKc(getDecimal(matched, "EPSKCJB"));
            data.setEpsXs(getDecimal(matched, "EPSXS"));
            data.setBps(getDecimal(matched, "BPS"));
            data.setMgzbgj(getDecimal(matched, "MGZBGJ"));
            data.setMgwfplr(getDecimal(matched, "MGWFPLR"));
            data.setMgjyxjje(getDecimal(matched, "MGJYXJJE"));

            // 营收利润
            data.setTotalRevenue(getDecimal(matched, "TOTALOPERATEREVE"));
            data.setGrossProfit(getDecimal(matched, "MLR"));
            data.setNetProfit(getDecimal(matched, "PARENTNETPROFIT"));
            data.setNetProfitKc(getDecimal(matched, "KCFJCXSYJLR"));

            // 增长率
            data.setRevenueYoy(getDecimal(matched, "TOTALOPERATEREVETZ"));
            data.setNetProfitYoy(getDecimal(matched, "PARENTNETPROFITTZ"));
            data.setNetProfitKcYoy(getDecimal(matched, "KCFJCXSYJLRTZ"));
            data.setRevenueQoq(getDecimal(matched, "YYZSRGDHBZC"));
            data.setNetProfitQoq(getDecimal(matched, "NETPROFITRPHBZC"));
            data.setNetProfitKcQoq(getDecimal(matched, "KFJLRGDHBZC"));

            // 盈利能力
            data.setRoe(getDecimal(matched, "ROEJQ"));
            data.setRoeKc(getDecimal(matched, "ROEKCJQ"));
            data.setRoa(getDecimal(matched, "ZZCJLL"));
            data.setNetProfitMargin(getDecimal(matched, "XSJLL"));
            data.setGrossProfitMargin(getDecimal(matched, "XSMLL"));

            // 运营能力
            data.setCashFlowRatio(getDecimal(matched, "JYXJLYYSR"));
            data.setTaxRate(getDecimal(matched, "TAXRATE"));

            // 偿债能力
            data.setCurrentRatio(getDecimal(matched, "LD"));
            data.setQuickRatio(getDecimal(matched, "SD"));
            data.setCashRatioVal(getDecimal(matched, "XJLLB"));
            data.setDebtRatio(getDecimal(matched, "ZCFZL"));
            data.setEquityMultiplier(getDecimal(matched, "QYCS"));

            // 周转能力
            data.setTotalAssetTurnover(getDecimal(matched, "ZZCZZTS"));
            data.setInventoryTurnover(getDecimal(matched, "CHZZTS"));
            data.setReceivableTurnover(getDecimal(matched, "YSZKZZTS"));

            // 保存数据
            Optional<StockFinanceData> existing = stockFinanceDataRepository
                    .findByStockCodeAndReportPeriod(stockCode, reportPeriod);
            existing.ifPresent(stockFinanceData -> data.setId(stockFinanceData.getId()));
            stockFinanceDataRepository.save(data);

            log.info("成功保存 {} ({}) 的财务数据", stockName, stockCode);
            return CrawlResult.success(stockCode, stockName, "获取成功并保存到数据库");

        } catch (Exception e) {
            log.error("爬取 {} 失败: {}", stockCode, e.getMessage(), e);
            return CrawlResult.fail(stockCode, "未知", e.getMessage());
        }
    }

    /**
     * 将报告期转换为日期前缀用于匹配
     */
    private String convertReportPeriodToDate(String reportPeriod) {
        String year = reportPeriod.substring(0, 4);
        String quarter = reportPeriod.substring(4);
        return switch (quarter) {
            case "Q1" -> year + "-03-31";
            case "Q2" -> year + "-06-30";
            case "Q3" -> year + "-09-30";
            case "Q4" -> year + "-12-31";
            default -> year + "-12-31";
        };
    }

    /**
     * 将日期转换回报告期格式
     */
    private String convertDateToReportPeriod(String date) {
        // date format: 2026-03-31
        if (date == null || date.length() < 10) return "未知";
        String year = date.substring(0, 4);
        String monthDay = date.substring(5, 10);
        return switch (monthDay) {
            case "03-31" -> year + "Q1";
            case "06-30" -> year + "Q2";
            case "09-30" -> year + "Q3";
            case "12-31" -> year + "Q4";
            default -> year + "Q4";
        };
    }

    /**
     * 发送GET请求
     */
    private String doGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://datacenter.eastmoney.com/")
                    .header("Accept", "application/json, text/plain, */*")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.warn("请求失败，状态码: {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("HTTP请求异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JSON节点安全获取BigDecimal值
     */
    private BigDecimal getDecimal(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            try {
                String value = node.get(fieldName).asText().trim();
                if (value.isEmpty() || value.equals("null") || value.equals("-")) {
                    return null;
                }
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从JSON节点安全获取文本值
     */
    private String getText(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    /**
     * 爬取结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CrawlResult {
        private boolean success;
        private String stockCode;
        private String stockName;
        private String message;

        public static CrawlResult success(String stockCode, String stockName, String message) {
            return new CrawlResult(true, stockCode, stockName, message);
        }

        public static CrawlResult fail(String stockCode, String stockName, String message) {
            return new CrawlResult(false, stockCode, stockName, message);
        }
    }
}
