package com.example.demo.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * 股票实时行情WebSocket处理器
 *
 * 前端通过WebSocket连接后发送要订阅的股票代码列表，
 * 后端定时从东方财富获取实时数据并推送给前端。
 *
 * @author : wangyuzhi
 */
@Component
@Slf4j
public class StockRealtimeHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** 每个session对应一个定时推送任务 */
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接建立: {}", session.getId());
    }

    /**
     * 接收前端消息
     * 格式: {"action": "subscribe", "stockCodes": ["601398","600036","000001"]}
     * 或:   {"action": "unsubscribe"}
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode msg = objectMapper.readTree(message.getPayload());
            String action = msg.path("action").asText();

            if ("subscribe".equals(action)) {
                // 取消之前的任务
                cancelTask(session.getId());

                // 解析股票代码列表
                List<String> stockCodes = new ArrayList<>();
                JsonNode codesNode = msg.path("stockCodes");
                if (codesNode.isArray()) {
                    for (JsonNode code : codesNode) {
                        stockCodes.add(code.asText());
                    }
                }

                if (stockCodes.isEmpty()) {
                    session.sendMessage(new TextMessage("{\"error\":\"stockCodes不能为空\"}"));
                    return;
                }

                log.info("Session {} 订阅 {} 只股票实时数据", session.getId(), stockCodes.size());

                // 启动定时推送（每3秒一次）
                ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
                    pushRealtimeData(session, stockCodes);
                }, 0, 3, TimeUnit.SECONDS);

                taskMap.put(session.getId(), future);

            } else if ("unsubscribe".equals(action)) {
                cancelTask(session.getId());
                log.info("Session {} 取消订阅", session.getId());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelTask(session.getId());
        log.info("WebSocket连接关闭: {}, status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        cancelTask(session.getId());
        log.error("WebSocket传输错误: {}", exception.getMessage());
    }

    /**
     * 从东方财富获取实时数据并推送
     */
    private void pushRealtimeData(WebSocketSession session, List<String> stockCodes) {
        if (!session.isOpen()) {
            cancelTask(session.getId());
            return;
        }

        try {
            // 构建secids参数
            StringBuilder secids = new StringBuilder();
            for (String code : stockCodes) {
                if (secids.length() > 0) secids.append(",");
                String market = code.startsWith("6") ? "1" : "0";
                secids.append(market).append(".").append(code);
            }

            String url = "http://push2.eastmoney.com/api/qt/ulist.np/get?"
                    + "fltt=2&invt=2"
                    + "&fields=f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f115"
                    + "&secids=" + secids;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://quote.eastmoney.com/")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return;

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode diff = root.path("data").path("diff");

            if (!diff.isArray()) return;

            // 构建推送数据
            List<Map<String, Object>> stockList = new ArrayList<>();
            for (JsonNode item : diff) {
                Map<String, Object> stock = new LinkedHashMap<>();
                stock.put("stockCode", item.path("f12").asText());
                stock.put("stockName", item.path("f14").asText());
                stock.put("price", item.path("f2").asDouble());
                stock.put("changePercent", item.path("f3").asDouble());
                stock.put("changeAmount", item.path("f4").asDouble());
                stock.put("volume", item.path("f5").asLong());         // 成交量（手）
                stock.put("turnover", item.path("f6").asDouble());     // 成交额
                stock.put("amplitude", item.path("f7").asDouble());    // 振幅%
                stock.put("turnoverRate", item.path("f8").asDouble()); // 换手率%
                stock.put("pe", item.path("f9").asDouble());           // 动态PE
                stock.put("volumeRatio", item.path("f10").asDouble()); // 量比
                stock.put("high", item.path("f15").asDouble());        // 最高
                stock.put("low", item.path("f16").asDouble());         // 最低
                stock.put("open", item.path("f17").asDouble());        // 开盘
                stock.put("prevClose", item.path("f18").asDouble());   // 昨收
                stock.put("totalMarketCap", item.path("f20").asLong());  // 总市值
                stock.put("circulationMarketCap", item.path("f21").asLong()); // 流通市值
                stock.put("pb", item.path("f23").asDouble());          // PB
                stock.put("change60d", item.path("f24").asDouble());   // 60日涨跌%
                stock.put("changeYtd", item.path("f25").asDouble());   // 年初至今%
                stock.put("dividendYield", item.path("f115").asDouble()); // 股息率%
                stockList.add(stock);
            }

            Map<String, Object> pushData = new LinkedHashMap<>();
            pushData.put("timestamp", System.currentTimeMillis());
            pushData.put("count", stockList.size());
            pushData.put("stocks", stockList);

            String json = objectMapper.writeValueAsString(pushData);
            session.sendMessage(new TextMessage(json));

        } catch (Exception e) {
            log.debug("推送实时数据异常: {}", e.getMessage());
        }
    }

    private void cancelTask(String sessionId) {
        ScheduledFuture<?> future = taskMap.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }
    }
}
