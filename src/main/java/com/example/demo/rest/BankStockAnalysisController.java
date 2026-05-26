package com.example.demo.rest;

import com.example.demo.entity.BankStock;
import com.example.demo.entity.BankStockData;
import com.example.demo.entity.BankStockScore;
import com.example.demo.entity.ModelWeight;
import com.example.demo.repository.BankStockDataRepository;
import com.example.demo.service.BankStockAnalysisService;
import com.example.demo.service.EastMoneyCrawlerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于数据库数据的银行股分析接口
 * 选择股票 + 选择报告期，直接从数据库取数据进行分析排名
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/bank-stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BankStockAnalysisController {

    private final BankStockDataRepository bankStockDataRepository;
    private final BankStockAnalysisService bankStockAnalysisService;
    private final EastMoneyCrawlerService eastMoneyCrawlerService;

    /**
     * 基于数据库数据进行分析
     * 前端传入股票代码列表 + 报告期 + 权重，后端从数据库取数据后计算排名
     * PB/PE/股息率从行情接口实时获取
     */
    @PostMapping("/analyze-db")
    public List<BankStockScore> analyzeFromDb(@RequestBody AnalyzeDbRequest request) {
        // 实时获取估值数据（PB/PE/股息率）
        Map<String, EastMoneyCrawlerService.ValuationData> valuationMap =
                eastMoneyCrawlerService.fetchRealtimeValuation(request.getStockCodes());

        List<BankStock> bankStocks = new ArrayList<>();

        for (String stockCode : request.getStockCodes()) {
            var dataOpt = bankStockDataRepository.findByStockCodeAndReportPeriod(stockCode, request.getReportPeriod());
            if (dataOpt.isPresent()) {
                BankStockData data = dataOpt.get();

                // 获取实时估值
                EastMoneyCrawlerService.ValuationData valuation = valuationMap.get(stockCode);
                Double pb = valuation != null ? valuation.getPb() : null;
                Double pe = valuation != null ? valuation.getPe() : null;
                Double dividendYield = valuation != null ? valuation.getDividendYield() : null;

                BankStock bankStock = BankStock.builder()
                        .stockCode(data.getStockCode())
                        .stockName(data.getStockName())
                        .currentPrice(null)
                        .roe(toDouble(data.getRoe()))
                        .nim(toDouble(data.getNim()))
                        .costIncomeRatio(toDouble(data.getCostIncomeRatio()))
                        .nplRatio(toDouble(data.getNplRatio()))
                        .provisionCoverageRatio(toDouble(data.getProvisionCoverageRatio()))
                        .specialMentionLoanRatio(toDouble(data.getSpecialMentionLoanRatio()))
                        .revenueGrowthRate(toDouble(data.getRevenueGrowthRate()))
                        .netProfitGrowthRate(toDouble(data.getNetProfitGrowthRate()))
                        .loanGrowthRate(toDouble(data.getLoanGrowthRate()))
                        .pb(pb)
                        .pe(pe)
                        .dividendYield(dividendYield)
                        .coreCapitalAdequacyRatio(toDouble(data.getCoreCapitalAdequacyRatio()))
                        .capitalAdequacyRatio(toDouble(data.getCapitalAdequacyRatio()))
                        .build();
                bankStocks.add(bankStock);
            }
        }

        if (bankStocks.size() < 2) {
            return List.of();
        }

        ModelWeight weight = request.getWeight() != null ? request.getWeight() : ModelWeight.defaultWeight();
        return bankStockAnalysisService.analyze(bankStocks, weight);
    }

    /**
     * 获取指定报告期有数据的所有股票列表（用于前端选择）
     */
    @GetMapping("/available-stocks")
    public List<StockOption> getAvailableStocks(@RequestParam("reportPeriod") String reportPeriod) {
        List<BankStockData> dataList = bankStockDataRepository.findByReportPeriod(reportPeriod);
        return dataList.stream()
                .map(d -> new StockOption(d.getStockCode(), d.getStockName()))
                .toList();
    }

    private Double toDouble(java.math.BigDecimal val) {
        return val != null ? val.doubleValue() : null;
    }

    @Data
    public static class AnalyzeDbRequest {
        private List<String> stockCodes;
        private String reportPeriod;
        private ModelWeight weight;
    }

    @Data
    @lombok.AllArgsConstructor
    public static class StockOption {
        private String stockCode;
        private String stockName;
    }
}
