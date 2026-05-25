package com.example.demo.service;

import cn.idev.excel.FastExcel;
import com.example.demo.entity.BankStock;
import com.example.demo.entity.BankStockScore;
import com.example.demo.entity.ModelWeight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 银行股价值分析服务
 *
 * 数学模型：S = w1*P + w2*Q + w3*G + w4*V + w5*C
 * - P: 盈利能力得分 (Profitability)
 * - Q: 资产质量得分 (Asset Quality)
 * - G: 成长性得分 (Growth)
 * - V: 估值/安全边际得分 (Valuation)
 * - C: 资本充足性得分 (Capital)
 *
 * 评分方法：百分位排名法，将各指标在所有银行中排名后转换为0-100分
 *
 * @author : wangyuzhi
 */
@Service
@Slf4j
public class BankStockAnalysisService {

    /**
     * 从Excel导入银行股数据并进行分析评分
     *
     * @param file Excel文件（包含银行股各项指标数据）
     * @param weight 权重配置，为null时使用默认权重
     * @return 评分排名结果列表
     */
    public List<BankStockScore> analyzeFromExcel(MultipartFile file, ModelWeight weight) {
        if (weight == null) {
            weight = ModelWeight.defaultWeight();
        }

        try {
            // 读取Excel数据
            BaseExcelListener<BankStock> listener = new BaseExcelListener<>();
            FastExcel.read(file.getInputStream(), BankStock.class, listener).sheet(0).doRead();
            List<BankStock> bankStocks = listener.getDataList();

            if (CollectionUtils.isEmpty(bankStocks)) {
                log.warn("Excel中无有效数据");
                return Collections.emptyList();
            }

            log.info("成功读取 {} 家银行数据，开始分析...", bankStocks.size());
            return analyze(bankStocks, weight);

        } catch (Exception e) {
            log.error("分析银行股数据失败", e);
            throw new RuntimeException("分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 直接传入数据进行分析（适用于API调用或程序化数据）
     *
     * @param bankStocks 银行股数据列表
     * @param weight 权重配置
     * @return 评分排名结果列表
     */
    public List<BankStockScore> analyze(List<BankStock> bankStocks, ModelWeight weight) {
        if (weight == null) {
            weight = ModelWeight.defaultWeight();
        }

        int n = bankStocks.size();
        if (n < 2) {
            log.warn("至少需要2家银行才能进行比较分析");
            return Collections.emptyList();
        }

        // 第一步：计算各指标的百分位排名得分
        Map<String, double[]> scoreMap = calculatePercentileScores(bankStocks);

        // 第二步：计算各维度综合得分
        List<BankStockScore> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            BankStock stock = bankStocks.get(i);
            BankStockScore score = calculateDimensionScores(stock, scoreMap, i, weight);
            results.add(score);
        }

        // 第三步：按综合得分排序
        results.sort(Comparator.comparing(BankStockScore::getTotalScore).reversed());

        // 第四步：设置排名
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        // 输出结果
        printResults(results, weight);

        return results;
    }

    /**
     * 计算所有指标的百分位排名得分
     * 使用百分位排名法：score = (rank - 1) / (N - 1) * 100
     */
    private Map<String, double[]> calculatePercentileScores(List<BankStock> bankStocks) {
        int n = bankStocks.size();
        Map<String, double[]> scoreMap = new HashMap<>();

        // 正向指标（越大越好）
        scoreMap.put("roe", percentileRank(bankStocks, BankStock::getRoe, true));
        scoreMap.put("nim", percentileRank(bankStocks, BankStock::getNim, true));
        scoreMap.put("provisionCoverage", percentileRank(bankStocks, BankStock::getProvisionCoverageRatio, true));
        scoreMap.put("revenueGrowth", percentileRank(bankStocks, BankStock::getRevenueGrowthRate, true));
        scoreMap.put("netProfitGrowth", percentileRank(bankStocks, BankStock::getNetProfitGrowthRate, true));
        scoreMap.put("loanGrowth", percentileRank(bankStocks, BankStock::getLoanGrowthRate, true));
        scoreMap.put("dividendYield", percentileRank(bankStocks, BankStock::getDividendYield, true));
        scoreMap.put("coreCapital", percentileRank(bankStocks, BankStock::getCoreCapitalAdequacyRatio, true));
        scoreMap.put("totalCapital", percentileRank(bankStocks, BankStock::getCapitalAdequacyRatio, true));

        // 反向指标（越小越好）
        scoreMap.put("costIncomeRatio", percentileRank(bankStocks, BankStock::getCostIncomeRatio, false));
        scoreMap.put("nplRatio", percentileRank(bankStocks, BankStock::getNplRatio, false));
        scoreMap.put("specialMention", percentileRank(bankStocks, BankStock::getSpecialMentionLoanRatio, false));
        scoreMap.put("pb", percentileRank(bankStocks, BankStock::getPb, false));
        scoreMap.put("pe", percentileRank(bankStocks, BankStock::getPe, false));

        return scoreMap;
    }

    /**
     * 百分位排名计算
     *
     * @param bankStocks 银行列表
     * @param extractor 指标提取函数
     * @param ascending true=越大越好，false=越小越好
     * @return 每家银行在该指标上的得分数组(0-100)
     */
    private double[] percentileRank(List<BankStock> bankStocks,
                                    java.util.function.Function<BankStock, Double> extractor,
                                    boolean ascending) {
        int n = bankStocks.size();
        double[] scores = new double[n];

        // 提取值并创建索引对
        List<double[]> indexedValues = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Double value = extractor.apply(bankStocks.get(i));
            // 空值处理：给予中位数排名
            indexedValues.add(new double[]{i, value != null ? value : Double.NaN});
        }

        // 按值排序（处理NaN）
        if (ascending) {
            indexedValues.sort((a, b) -> {
                if (Double.isNaN(a[1]) && Double.isNaN(b[1])) return 0;
                if (Double.isNaN(a[1])) return -1;
                if (Double.isNaN(b[1])) return 1;
                return Double.compare(a[1], b[1]);
            });
        } else {
            // 反向：值越小排名越高
            indexedValues.sort((a, b) -> {
                if (Double.isNaN(a[1]) && Double.isNaN(b[1])) return 0;
                if (Double.isNaN(a[1])) return -1;
                if (Double.isNaN(b[1])) return 1;
                return Double.compare(b[1], a[1]);
            });
        }

        // 分配排名得分
        for (int rank = 0; rank < n; rank++) {
            int originalIndex = (int) indexedValues.get(rank)[0];
            if (Double.isNaN(indexedValues.get(rank)[1])) {
                // 空值给50分（中位数）
                scores[originalIndex] = 50.0;
            } else if (n == 1) {
                scores[originalIndex] = 50.0;
            } else {
                scores[originalIndex] = (double) rank / (n - 1) * 100.0;
            }
        }

        return scores;
    }

    /**
     * 计算各维度得分和综合得分
     */
    private BankStockScore calculateDimensionScores(BankStock stock,
                                                     Map<String, double[]> scoreMap,
                                                     int index,
                                                     ModelWeight w) {
        // 盈利能力得分
        double profitability = w.getRoeWeight() * scoreMap.get("roe")[index]
                + w.getNimWeight() * scoreMap.get("nim")[index]
                + w.getCostIncomeRatioWeight() * scoreMap.get("costIncomeRatio")[index];

        // 资产质量得分
        double assetQuality = w.getNplRatioWeight() * scoreMap.get("nplRatio")[index]
                + w.getProvisionCoverageWeight() * scoreMap.get("provisionCoverage")[index]
                + w.getSpecialMentionWeight() * scoreMap.get("specialMention")[index];

        // 成长性得分
        double growth = w.getRevenueGrowthWeight() * scoreMap.get("revenueGrowth")[index]
                + w.getNetProfitGrowthWeight() * scoreMap.get("netProfitGrowth")[index]
                + w.getLoanGrowthWeight() * scoreMap.get("loanGrowth")[index];

        // 估值/安全边际得分
        double valuation = w.getPbWeight() * scoreMap.get("pb")[index]
                + w.getPeWeight() * scoreMap.get("pe")[index]
                + w.getDividendYieldWeight() * scoreMap.get("dividendYield")[index];

        // 资本充足性得分
        double capital = w.getCoreCapitalWeight() * scoreMap.get("coreCapital")[index]
                + w.getTotalCapitalWeight() * scoreMap.get("totalCapital")[index];

        // 综合得分
        double totalScore = w.getProfitabilityWeight() * profitability
                + w.getAssetQualityWeight() * assetQuality
                + w.getGrowthWeight() * growth
                + w.getValuationWeight() * valuation
                + w.getCapitalWeight() * capital;

        return BankStockScore.builder()
                .stockCode(stock.getStockCode())
                .stockName(stock.getStockName())
                .currentPrice(stock.getCurrentPrice())
                .profitabilityScore(round(profitability))
                .assetQualityScore(round(assetQuality))
                .growthScore(round(growth))
                .valuationScore(round(valuation))
                .capitalScore(round(capital))
                .totalScore(round(totalScore))
                .roe(stock.getRoe())
                .nim(stock.getNim())
                .nplRatio(stock.getNplRatio())
                .provisionCoverageRatio(stock.getProvisionCoverageRatio())
                .netProfitGrowthRate(stock.getNetProfitGrowthRate())
                .pb(stock.getPb())
                .dividendYield(stock.getDividendYield())
                .coreCapitalAdequacyRatio(stock.getCoreCapitalAdequacyRatio())
                .build();
    }

    /**
     * 打印分析结果
     */
    private void printResults(List<BankStockScore> results, ModelWeight weight) {
        log.info("====================================================");
        log.info("          银行股价值分析报告");
        log.info("====================================================");
        log.info("模型权重配置：盈利能力={}, 资产质量={}, 成长性={}, 估值={}, 资本充足={}",
                weight.getProfitabilityWeight(), weight.getAssetQualityWeight(),
                weight.getGrowthWeight(), weight.getValuationWeight(), weight.getCapitalWeight());
        log.info("----------------------------------------------------");
        log.info(String.format("%-4s %-8s %-6s %-6s %-6s %-6s %-6s %-6s %-8s",
                "排名", "名称", "综合分", "盈利", "质量", "成长", "估值", "资本", "股息率"));
        log.info("----------------------------------------------------");

        for (BankStockScore score : results) {
            log.info(String.format("%-4d %-8s %-6.1f %-6.1f %-6.1f %-6.1f %-6.1f %-6.1f %-8.2f%%",
                    score.getRank(),
                    score.getStockName(),
                    score.getTotalScore(),
                    score.getProfitabilityScore(),
                    score.getAssetQualityScore(),
                    score.getGrowthScore(),
                    score.getValuationScore(),
                    score.getCapitalScore(),
                    score.getDividendYield() != null ? score.getDividendYield() : 0.0));
        }

        log.info("====================================================");
        log.info("推荐关注 Top 5：");
        results.stream().limit(5).forEach(s ->
                log.info("  {} ({}) - 综合得分: {}, PB: {}, ROE: {}%, 股息率: {}%",
                        s.getStockName(), s.getStockCode(), s.getTotalScore(),
                        s.getPb(), s.getRoe(), s.getDividendYield()));
        log.info("====================================================");
    }

    /**
     * 保留两位小数
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
