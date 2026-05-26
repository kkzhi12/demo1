package com.example.demo.service;

import com.example.demo.entity.BankStockData;
import com.example.demo.repository.BankStockDataRepository;
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
import java.util.*;

/**
 * 东方财富数据爬虫服务
 * 通过东方财富公开接口获取银行股财务指标数据
 *
 * @author : wangyuzhi
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EastMoneyCrawlerService {

    private final BankStockDataRepository bankStockDataRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 常见银行股代码映射
     */
    private static final Map<String, String> BANK_STOCK_MAP = new LinkedHashMap<>() {{
        put("601398", "工商银行");
        put("601939", "建设银行");
        put("601288", "农业银行");
        put("601988", "中国银行");
        put("601328", "交通银行");
        put("600036", "招商银行");
        put("600016", "民生银行");
        put("601166", "兴业银行");
        put("600000", "浦发银行");
        put("000001", "平安银行");
        put("002142", "宁波银行");
        put("601818", "光大银行");
        put("600015", "华夏银行");
        put("601998", "中信银行");
        put("601009", "南京银行");
        put("600926", "杭州银行");
        put("601128", "常熟银行");
        put("600908", "无锡银行");
        put("002839", "张家港行");
        put("601838", "成都银行");
        put("601577", "长沙银行");
        put("601860", "紫金银行");
        put("600919", "江苏银行");
        put("002948", "青岛银行");
        put("601169", "北京银行");
        put("002807", "江阴银行");
        put("601963", "重庆银行");
        put("600928", "西安银行");
        put("601528", "瑞丰银行");
        put("601916", "浙商银行");
        put("002966", "苏州银行");
        put("601825", "沪农商行");
        put("601077", "渝农商行");
    }};

    /**
     * 爬取指定股票的财务指标数据
     *
     * @param stockCode    股票代码（如 601398）
     * @param reportPeriod 报告期（如 2024Q4）
     * @return 爬取结果
     */
    @Transactional
    public CrawlResult crawlSingleStock(String stockCode, String reportPeriod) {
        try {
            String stockName = BANK_STOCK_MAP.getOrDefault(stockCode, "未知");
            log.info("开始爬取 {} ({}) 的财务数据...", stockName, stockCode);

            // 确定市场后缀
            String marketSuffix = stockCode.startsWith("6") ? "SH" : "SZ";
            String secuCode = stockCode + "." + marketSuffix;

            // 调用东方财富 datacenter 接口
            BankStockData data = fetchFinancialData(secuCode, stockCode, stockName, reportPeriod);

            if (data != null) {
                // 检查是否已存在
                Optional<BankStockData> existing = bankStockDataRepository
                        .findByStockCodeAndReportPeriod(stockCode, reportPeriod);
                if (existing.isPresent()) {
                    data.setId(existing.get().getId());
                }
                bankStockDataRepository.save(data);
                log.info("成功保存 {} 的数据", stockName);
                return CrawlResult.success(stockCode, stockName, "爬取成功并保存到数据库");
            } else {
                return CrawlResult.fail(stockCode, stockName, "该报告期（" + reportPeriod + "）数据尚未发布");
            }

        } catch (Exception e) {
            log.error("爬取 {} 失败: {}", stockCode, e.getMessage(), e);
            return CrawlResult.fail(stockCode, BANK_STOCK_MAP.getOrDefault(stockCode, "未知"), e.getMessage());
        }
    }

    /**
     * 批量爬取所有银行股数据
     *
     * @param reportPeriod 报告期
     * @return 爬取结果列表
     */
    @Transactional
    public List<CrawlResult> crawlAllBanks(String reportPeriod) {
        List<CrawlResult> results = new ArrayList<>();
        for (String stockCode : BANK_STOCK_MAP.keySet()) {
            CrawlResult result = crawlSingleStock(stockCode, reportPeriod);
            results.add(result);
            // 请求间隔，避免被封
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return results;
    }

    /**
     * 通过东方财富 datacenter 接口获取银行财务数据
     * 接口地址：https://datacenter.eastmoney.com/securities/api/data/v1/get
     * reportName=RPT_F10_FINANCE_MAINFINADATA（主要财务指标）
     */
    private BankStockData fetchFinancialData(String secuCode, String stockCode, String stockName, String reportPeriod) {
        try {
            String apiUrl = "https://datacenter.eastmoney.com/securities/api/data/v1/get?"
                    + "reportName=RPT_F10_FINANCE_MAINFINADATA"
                    + "&columns=ALL"
                    + "&filter=(SECUCODE%3D%22" + secuCode + "%22)"
                    + "&pageNumber=1&pageSize=20"
                    + "&sortTypes=-1&sortColumns=REPORT_DATE";

            log.info("请求URL: {}", apiUrl);
            String json = doGet(apiUrl);

            if (json == null || json.isBlank()) {
                log.warn("接口返回为空: {}", stockCode);
                return null;
            }

            JsonNode root = objectMapper.readTree(json);

            // 检查返回结构
            if (!root.has("result") || root.get("result").isNull()) {
                log.warn("接口返回无result: {}", stockCode);
                return null;
            }

            JsonNode result = root.get("result");
            JsonNode dataArray = result.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
                log.warn("接口返回无数据: {}", stockCode);
                return null;
            }

            // 根据 reportPeriod 匹配对应的报告期数据
            // reportPeriod 格式: 2024Q4, 2025Q1 等
            // 接口返回的 REPORT_DATE 格式: "2024-12-31 00:00:00", "2025-03-31 00:00:00"
            String targetDate = convertReportPeriodToDate(reportPeriod);
            log.info("目标报告期: {} -> {}", reportPeriod, targetDate);

            JsonNode matched = null;
            for (JsonNode item : dataArray) {
                String reportDate = item.has("REPORT_DATE") ? item.get("REPORT_DATE").asText() : "";
                if (reportDate.startsWith(targetDate)) {
                    matched = item;
                    break;
                }
            }

            if (matched == null) {
                log.warn("未找到匹配的报告期数据: {} - {}", stockCode, reportPeriod);
                return null;
            }

            BankStockData data = new BankStockData();
            data.setStockCode(stockCode);
            data.setStockName(stockName);
            data.setReportPeriod(reportPeriod);
            data.setImportTime(LocalDateTime.now());
            data.setDataSource("东方财富爬取");

            // 映射字段 - 根据东方财富实际返回的字段名
            // 盈利能力
            data.setRoe(getDecimal(matched, "ROEJQ"));                          // ROE(加权) 2.2075
            data.setNim(getDecimal(matched, "NET_INTEREST_MARGIN"));            // 净息差 1.29
            data.setCostIncomeRatio(getDecimal(matched, "COST_INCOME_RATIO"));  // 成本收入比（可能为null）

            // 资产质量
            data.setNplRatio(getDecimal(matched, "NONPERLOAN"));                // 不良贷款率 1.31
            data.setProvisionCoverageRatio(getDecimal(matched, "BLDKBBL"));     // 拨备覆盖率 214.38
            data.setSpecialMentionLoanRatio(getDecimal(matched, "SPECIAL_MENTION_LOAN_RATIO")); // 关注类占比

            // 成长性
            data.setRevenueGrowthRate(getDecimal(matched, "TOTALOPERATERETZ")); // 营收同比增长
            if (data.getRevenueGrowthRate() == null) {
                data.setRevenueGrowthRate(getDecimal(matched, "TOTALOPERATEREVETZ"));
            }
            if (data.getRevenueGrowthRate() == null) {
                data.setRevenueGrowthRate(getDecimal(matched, "OI_YOYRATIO_PK"));
            }

            data.setNetProfitGrowthRate(getDecimal(matched, "PARENTNETPROFITTZ")); // 归母净利润同比 3.31
            data.setLoanGrowthRate(getDecimal(matched, "LOAN_GROWTH_RATE"));       // 贷款增长率

            // 估值（财务报表接口里没有PB/PE/股息率，后续可从行情接口补充）
            data.setPb(getDecimal(matched, "PB"));
            data.setPe(getDecimal(matched, "PE"));
            data.setDividendYield(getDecimal(matched, "DIVIDEND_YIELD"));

            // 资本充足性
            data.setCapitalAdequacyRatio(getDecimal(matched, "NEWCAPITALADER"));        // 资本充足率 18.21
            data.setCoreCapitalAdequacyRatio(getDecimal(matched, "HXYJBCZL"));          // 核心一级资本充足率 13.26
            if (data.getCoreCapitalAdequacyRatio() == null) {
                data.setCoreCapitalAdequacyRatio(getDecimal(matched, "FIRST_ADEQUACY_RATIO"));
            }

            // 打印获取到的数据
            log.info("爬取结果 - {} ROE:{}, 营收增长:{}, 净利润增长:{}, 不良率:{}, 拨备覆盖率:{}, 资本充足率:{}",
                    stockName, data.getRoe(), data.getRevenueGrowthRate(),
                    data.getNetProfitGrowthRate(), data.getNplRatio(),
                    data.getProvisionCoverageRatio(), data.getCapitalAdequacyRatio());

            return data;

        } catch (Exception e) {
            log.error("获取财务数据失败: {} - {}", stockCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将报告期转换为日期前缀用于匹配
     * 2024Q1 -> 2024-03-31
     * 2024Q2 -> 2024-06-30
     * 2024Q3 -> 2024-09-30
     * 2024Q4 -> 2024-12-31
     */
    private String convertReportPeriodToDate(String reportPeriod) {
        // 格式: 2024Q4
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
                log.warn("请求失败，状态码: {}, URL: {}", response.statusCode(), url);
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
     * 获取支持的银行股列表（静态配置）
     */
    public Map<String, String> getSupportedBanks() {
        return BANK_STOCK_MAP;
    }

    /**
     * 搜索股票（支持代码或名称模糊匹配）
     * 从东方财富行情接口搜索，支持所有A股
     *
     * @param keyword 搜索关键字（代码或名称）
     * @return 匹配的股票列表（最多50条）
     */
    public List<Map<String, String>> searchStock(String keyword) {
        List<Map<String, String>> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return result;

        String url = "http://push2.eastmoney.com/api/qt/clist/get?"
                + "pn=1&pz=50&po=1&np=1&fltt=2&invt=2&fid=f3"
                + "&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23"
                + "&fields=f12,f14,f13"
                + "&fid0=f12&filter0=(f12+like+%22" + keyword + "%25%22)"
                + "|(f14+like+%22%" + keyword + "%25%22)";

        // 东方财富的搜索用另一个更简单的接口
        String searchUrl = "https://searchapi.eastmoney.com/api/suggest/get?"
                + "input=" + keyword
                + "&type=14&token=D43BF722C8E33BDC906FB84D85E326E8&count=20";

        try {
            String json = doGet(searchUrl);
            if (json == null || json.isBlank()) return result;

            JsonNode root = objectMapper.readTree(json);
            JsonNode dataNode = root.path("QuotationCodeTable").path("Data");

            if (dataNode.isArray()) {
                for (JsonNode item : dataNode) {
                    String code = item.path("Code").asText();
                    String name = item.path("Name").asText();
                    String market = item.path("MktNum").asText();

                    // 只保留沪深A股（MktNum: 33=深圳, 17=上海）
                    if ("33".equals(market) || "17".equals(market)) {
                        Map<String, String> stock = new LinkedHashMap<>();
                        stock.put("stockCode", code);
                        stock.put("stockName", name);
                        result.add(stock);
                    }
                }
            }
        } catch (Exception e) {
            log.error("搜索股票失败: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 实时获取指定股票的PB、PE、股息率（从行情接口）
     * 不入库，每次分析时实时调用
     *
     * @param stockCodes 股票代码列表
     * @return 股票代码 -> 估值数据Map
     */
    public Map<String, ValuationData> fetchRealtimeValuation(List<String> stockCodes) {
        Map<String, ValuationData> result = new HashMap<>();

        // 构建 secids 参数：1.601398,0.000001 (1=上海,0=深圳)
        StringBuilder secids = new StringBuilder();
        for (String code : stockCodes) {
            if (secids.length() > 0) secids.append(",");
            String market = code.startsWith("6") ? "1" : "0";
            secids.append(market).append(".").append(code);
        }

        String url = "http://push2.eastmoney.com/api/qt/ulist.np/get?"
                + "fltt=2&invt=2&fields=f12,f14,f23,f9,f115"
                + "&secids=" + secids;
        // f12=代码, f14=名称, f23=PB, f9=PE(动态), f115=股息率

        try {
            String json = doGet(url);
            if (json == null || json.isBlank()) {
                log.warn("获取实时估值数据为空");
                return result;
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("data").path("diff");

            if (data.isArray()) {
                for (JsonNode item : data) {
                    String code = item.path("f12").asText();
                    Double pb = getDoubleFromNode(item, "f23");
                    Double pe = getDoubleFromNode(item, "f9");
                    Double dividendYield = getDoubleFromNode(item, "f115");

                    result.put(code, new ValuationData(pb, pe, dividendYield));
                }
                log.info("成功获取 {} 只股票的实时估值数据", result.size());
            }

        } catch (Exception e) {
            log.error("获取实时估值数据失败: {}", e.getMessage());
        }

        return result;
    }

    private Double getDoubleFromNode(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            try {
                double val = node.get(field).asDouble();
                // 东方财富用 "-" 表示无数据，asDouble会返回0
                if (val == 0 && !node.get(field).asText().equals("0")) {
                    return null;
                }
                return val;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 实时估值数据
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ValuationData {
        private Double pb;
        private Double pe;
        private Double dividendYield;
    }

    /**
     * 从东方财富接口动态获取所有A股股票代码列表
     * 接口：push2.eastmoney.com/api/qt/clist/get
     *
     * @return 股票代码 -> 股票名称 的Map
     */
    public Map<String, String> fetchAllStockCodes() {
        Map<String, String> stockMap = new LinkedHashMap<>();
        int page = 1;
        int pageSize = 2000;

        while (true) {
            String url = "http://push2.eastmoney.com/api/qt/clist/get?"
                    + "pn=" + page
                    + "&pz=" + pageSize
                    + "&po=1&np=1&fltt=2&invt=2&fid=f3"
                    + "&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23"  // 沪深A股
                    + "&fields=f12,f14,f13";  // f12=代码, f14=名称, f13=市场

            try {
                String json = doGet(url);
                if (json == null || json.isBlank()) break;

                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data");
                if (data.isNull() || data.isMissingNode()) break;

                JsonNode diff = data.path("diff");
                if (!diff.isArray() || diff.isEmpty()) break;

                for (JsonNode item : diff) {
                    String code = item.path("f12").asText();
                    String name = item.path("f14").asText();
                    stockMap.put(code, name);
                }

                int total = data.path("total").asInt(0);
                log.info("获取股票列表第{}页，本页{}条，累计{}/{}",
                        page, diff.size(), stockMap.size(), total);

                if (stockMap.size() >= total) break;
                page++;
                Thread.sleep(300);

            } catch (Exception e) {
                log.error("获取股票列表第{}页失败: {}", page, e.getMessage());
                break;
            }
        }

        log.info("共获取 {} 只A股股票", stockMap.size());
        return stockMap;
    }

    /**
     * 从东方财富接口获取银行板块股票列表
     * 板块代码：BK0475（银行）
     *
     * @return 银行股代码 -> 名称 的Map
     */
    public Map<String, String> fetchBankStockCodes() {
        Map<String, String> bankMap = new LinkedHashMap<>();

        String url = "http://push2.eastmoney.com/api/qt/clist/get?"
                + "pn=1&pz=100&po=1&np=1&fltt=2&invt=2&fid=f3"
                + "&fs=b:BK0475"  // 银行板块
                + "&fields=f12,f14,f13";

        try {
            String json = doGet(url);
            if (json == null || json.isBlank()) {
                log.warn("获取银行板块数据为空，使用静态列表");
                return BANK_STOCK_MAP;
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode diff = root.path("data").path("diff");

            if (diff.isArray() && !diff.isEmpty()) {
                for (JsonNode item : diff) {
                    String code = item.path("f12").asText();
                    String name = item.path("f14").asText();
                    bankMap.put(code, name);
                }
                log.info("从东方财富接口获取到 {} 只银行股", bankMap.size());
            } else {
                log.warn("银行板块接口返回为空，使用静态列表");
                return BANK_STOCK_MAP;
            }

        } catch (Exception e) {
            log.error("获取银行板块失败: {}，使用静态列表", e.getMessage());
            return BANK_STOCK_MAP;
        }

        return bankMap;
    }

    /**
     * 使用动态获取的银行股列表批量爬取财报数据
     *
     * @param reportPeriod 报告期（如 2024Q4）
     * @return 爬取结果列表
     */
    @Transactional
    public List<CrawlResult> crawlAllBanksDynamic(String reportPeriod) {
        // 动态获取最新银行股列表
        Map<String, String> bankStocks = fetchBankStockCodes();
        log.info("使用动态银行股列表，共{}只", bankStocks.size());

        List<CrawlResult> results = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, String> entry : bankStocks.entrySet()) {
            index++;
            String stockCode = entry.getKey();
            String stockName = entry.getValue();

            log.info("[{}/{}] 正在爬取 {}({})...", index, bankStocks.size(), stockName, stockCode);

            try {
                // 确定市场后缀
                String marketSuffix = stockCode.startsWith("6") ? "SH" : "SZ";
                String secuCode = stockCode + "." + marketSuffix;

                BankStockData data = fetchFinancialData(secuCode, stockCode, stockName, reportPeriod);

                if (data != null) {
                    Optional<BankStockData> existing = bankStockDataRepository
                            .findByStockCodeAndReportPeriod(stockCode, reportPeriod);
                    existing.ifPresent(bankStockData -> data.setId(bankStockData.getId()));
                    bankStockDataRepository.save(data);
                    results.add(CrawlResult.success(stockCode, stockName, "爬取成功"));
                } else {
                    results.add(CrawlResult.fail(stockCode, stockName, "该报告期数据尚未发布"));
                }

            } catch (Exception e) {
                log.error("爬取 {}({}) 失败: {}", stockName, stockCode, e.getMessage());
                results.add(CrawlResult.fail(stockCode, stockName, e.getMessage()));
            }

            // 请求间隔
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        long successCount = results.stream().filter(CrawlResult::isSuccess).count();
        log.info("批量爬取完成：成功{}/总共{}", successCount, results.size());
        return results;
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
