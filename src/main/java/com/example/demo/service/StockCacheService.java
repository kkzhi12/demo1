package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 股票缓存服务
 * 服务启动时从东方财富加载所有A股代码和名称到内存缓存
 * 提供快速的本地搜索能力
 *
 * @author : wangyuzhi
 */
@Service
@Slf4j
public class StockCacheService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** 全量股票缓存 */
    private final List<StockItem> allStocks = new CopyOnWriteArrayList<>();

    /**
     * 服务启动时加载所有A股数据到缓存
     */
    @PostConstruct
    public void init() {
        log.info("开始加载A股股票列表到缓存...");
        new Thread(this::loadAllStocks, "stock-cache-loader").start();
    }

    private void loadAllStocks() {
        List<StockItem> stocks = new ArrayList<>();
        int page = 1;
        int pageSize = 5000;
        int total = Integer.MAX_VALUE;
        int maxRetry = 3;

        while (stocks.size() < total) {
            String url = "http://push2.eastmoney.com/api/qt/clist/get?"
                    + "pn=" + page
                    + "&pz=" + pageSize
                    + "&po=1&np=1&fltt=2&invt=2&fid=f3"
                    + "&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23"
                    + "&fields=f12,f14,f13";

            boolean pageSuccess = false;
            for (int retry = 1; retry <= maxRetry; retry++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .header("Referer", "https://quote.eastmoney.com/")
                            .timeout(Duration.ofSeconds(15))
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        log.warn("第{}页HTTP状态码{}，重试{}/{}", page, response.statusCode(), retry, maxRetry);
                        Thread.sleep(1000);
                        continue;
                    }

                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode data = root.path("data");
                    if (data.isNull() || data.isMissingNode()) {
                        total = stocks.size(); // 没有更多数据了
                        pageSuccess = true;
                        break;
                    }

                    JsonNode diff = data.path("diff");
                    if (!diff.isArray() || diff.isEmpty()) {
                        total = stocks.size();
                        pageSuccess = true;
                        break;
                    }

                    total = data.path("total").asInt(stocks.size());
                    for (JsonNode item : diff) {
                        String code = item.path("f12").asText();
                        String name = item.path("f14").asText();
                        stocks.add(new StockItem(code, name));
                    }

                    log.info("加载股票缓存第{}页，累计{}/{}", page, stocks.size(), total);
                    pageSuccess = true;
                    break;

                } catch (Exception e) {
                    log.warn("第{}页失败(重试{}/{}): {}", page, retry, maxRetry, e.getMessage());
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }

            if (!pageSuccess) {
                log.warn("第{}页重试{}次仍失败，跳过继续下一页", page, maxRetry);
            }

            page++;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        allStocks.clear();
        allStocks.addAll(stocks);
        log.info("股票缓存加载完成，共 {} 只A股", allStocks.size());
    }

    /**
     * 搜索股票（支持代码或名称模糊匹配）
     *
     * @param keyword 搜索关键字
     * @param limit   最大返回条数
     * @return 匹配的股票列表
     */
    public List<StockItem> search(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        String kw = keyword.trim().toLowerCase();

        return allStocks.stream()
                .filter(s -> s.getStockCode().contains(kw) || s.getStockName().toLowerCase().contains(kw))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取缓存中的股票总数
     */
    public int getCacheSize() {
        return allStocks.size();
    }

    /**
     * 获取所有股票（慎用，数据量大）
     */
    public List<StockItem> getAll() {
        return Collections.unmodifiableList(allStocks);
    }

    @Data
    @AllArgsConstructor
    public static class StockItem {
        private String stockCode;
        private String stockName;
    }
}
