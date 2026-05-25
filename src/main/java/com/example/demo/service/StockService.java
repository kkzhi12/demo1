package com.example.demo.service;

import cn.idev.excel.FastExcel;
import com.example.demo.entity.StockInfo;
import com.example.demo.entity.StockRoe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : wangyuzhi
 * @date: 2025/6/5  15:13
 */

@Service
@Slf4j
public class StockService {

    final Double stockDividendStrategy = 0.6;

    final Double stockRoeStrategy = 0.4;

    public void importExcel(MultipartFile file) {
        try {
            BaseExcelListener<StockInfo> stockInfoBaseExcelListener = new BaseExcelListener<>();
            FastExcel.read(file.getInputStream(), StockInfo.class, stockInfoBaseExcelListener).sheet(0).doRead();
            List<StockInfo> stockInfos = stockInfoBaseExcelListener.getDataList();
            if (CollectionUtils.isEmpty(stockInfos)) {
                log.info("无数据");
                return;
            }
            BaseExcelListener<StockRoe> stockRoeBaseExcelListener = new BaseExcelListener<>();
            FastExcel.read(file.getInputStream(), StockRoe.class, stockRoeBaseExcelListener).sheet(1).doRead();
            List<StockRoe> stockRoeList = stockRoeBaseExcelListener.getDataList();
            Map<String, StockRoe> stockRoeMap = stockRoeList.stream().collect(Collectors.toMap(StockRoe::getStockNumber, Function.identity()));
            double maxDividend = 0.0;
            double minDividend = 0.0;
            double maxRoe = 0.0;
            double minRoe = 0.0;
            for (StockInfo stockInfo : stockInfos) {
                StockRoe stockRoe = stockRoeMap.get(stockInfo.getStockNumber());
                String stockRoeComma = stockRoe.getStockRoeComma();
                String[] stockRoeArr = stockRoeComma.split(",");

                double stockRoeCount = 0.0;
                for (String s : stockRoeArr) {
                    stockRoeCount += Double.parseDouble(s);
                }
                double stockRoeAvg = stockRoeCount / stockRoeArr.length;
                stockInfo.setStockRoeAvg(stockRoeAvg);
                if (stockRoeAvg > maxRoe) {
                    maxRoe = stockRoeAvg;
                }
                if (stockRoeAvg < minRoe) {
                    minRoe = stockRoeAvg;
                }

                double stockDividendRate = stockInfo.getStockDividend() / stockInfo.getStockPrice();
                stockInfo.setStockDividendRate(stockDividendRate);
                if (stockDividendRate > maxDividend) {
                    maxDividend = stockDividendRate;
                }
                if (stockDividendRate < minDividend) {
                    minDividend = stockDividendRate;
                }
            }
            for (StockInfo stockInfo : stockInfos) {
                double normalizedStockDividend = (stockInfo.getStockDividendRate() - minDividend) / (maxDividend - minDividend);
                stockInfo.setNormalizedStockDividend(normalizedStockDividend);
                double normalizedStockRoe = (stockInfo.getStockRoeAvg() - minRoe) / (maxRoe - minRoe);
                stockInfo.setNormalizedStockRoe(normalizedStockRoe);
                double score = normalizedStockDividend * stockDividendStrategy + normalizedStockRoe + stockRoeStrategy;
                stockInfo.setScore(score);
            }
            stockInfos = stockInfos.stream().sorted(Comparator.comparing(StockInfo::getScore).reversed()).toList();
            int i = 1;
            for (StockInfo stockInfo : stockInfos) {
                log.info("根据提供的数据计算出：\n");
                String result = String.format("排名第%d的为, 股票名称为%s, 股票编码为%s。最终得分为%.3f\n" +
                        "平均roe为%.3f, 每股收益率为%.3f, 归一化的平均roe为%.3f, 归一化的每股收益率为%.3f\n", i, stockInfo.getStockName(), stockInfo.getStockNumber(), stockInfo.getScore(), stockInfo.getStockRoeAvg(), stockInfo.getStockDividendRate(), stockInfo.getNormalizedStockRoe(), stockInfo.getNormalizedStockDividend());
                log.info(result + "\n");
                i++;
            }
            log.info("本次的策略为，roe因素占比{}，stock dividend因素占比{}\n", stockRoeStrategy, stockDividendStrategy);
        } catch(Exception e) {
            if (log.isErrorEnabled()) {
                log.error("导入数据错误");
            }
        }
    }


}
