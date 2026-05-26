package com.example.demo.service;

import com.example.demo.entity.StockBasicInfo;
import com.example.demo.repository.StockBasicInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 股票缓存服务
 * 启动时从数据库加载，手动触发时从东方财富拉取并入库
 *
 * @author : wangyuzhi
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockCacheService {

    private final StockBasicInfoRepository stockBasicInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** 全量股票缓存 */
    private final List<StockItem> allStocks = new CopyOnWriteArrayList<>();

    /**
     * 服务启动时从数据库加载缓存
     */
    @PostConstruct
    public void init() {
        loadFromDb();
    }

    /**
     * 从数据库加载股票列表到内存
     */
    private void loadFromDb() {
        List<StockBasicInfo> list = stockBasicInfoRepository.findAll();
        if (!list.isEmpty()) {
            allStocks.clear();
            allStocks.addAll(list.stream()
                    .map(s -> new StockItem(s.getStockCode(), s.getStockName()))
                    .toList());
            log.info("从数据库加载股票缓存，共 {} 只", allStocks.size());
        } else {
            log.info("数据库中无股票基本信息，请手动调用接口加载");
        }
    }

    /**
     * 手动触发：从东方财富拉取所有A股并入库
     * 成功后自动刷新内存缓存
     *
     * @return 加载的股票数量
     */
    public int fetchAndSave() {
        log.info("开始从东方财富拉取A股列表...");
        List<StockItem> stocks = fetchFromEastMoney();

        if (stocks.isEmpty()) {
            log.error("从东方财富获取数据为空，可能被限流");
            return 0;
        }

        // 入库
        LocalDateTime now = LocalDateTime.now();
        List<StockBasicInfo> entities = stocks.stream()
                .map(s -> StockBasicInfo.builder()
                        .stockCode(s.getStockCode())
                        .stockName(s.getStockName())
                        .updatedAt(now)
                        .build())
                .toList();

        // 先清空再批量插入（简单粗暴但有效）
        stockBasicInfoRepository.deleteAll();
        stockBasicInfoRepository.saveAll(entities);
        log.info("成功保存 {} 只股票到数据库", entities.size());

        // 刷新内存缓存
        allStocks.clear();
        allStocks.addAll(stocks);

        return stocks.size();
    }

    /**
     * 从东方财富接口拉取所有A股
     */
    private List<StockItem> fetchFromEastMoney() {
        List<StockItem> stocks = new ArrayList<>();
        int page = 1;
        int total = Integer.MAX_VALUE;
        int maxRetry = 3;

        while (stocks.size() < total) {
            String url = "http://push2.eastmoney.com/api/qt/clist/get?"
                    + "pn=" + page
                    + "&pz=5000"
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
                        Thread.sleep(3000);
                        continue;
                    }

                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode data = root.path("data");
                    if (data.isNull() || data.isMissingNode()) {
                        total = stocks.size();
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

                    log.info("拉取第{}页，本页{}条，累计{}/{}", page, diff.size(), stocks.size(), total);
                    pageSuccess = true;
                    break;

                } catch (Exception e) {
                    log.warn("第{}页失败(重试{}/{}): {}", page, retry, maxRetry, e.getMessage());
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                }
            }

            if (!pageSuccess) {
                log.warn("第{}页重试{}次仍失败，跳过", page, maxRetry);
            }

            page++;
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }

        return stocks;
    }

    /**
     * 搜索股票（支持代码或名称模糊匹配）
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
     * 获取所有股票
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
