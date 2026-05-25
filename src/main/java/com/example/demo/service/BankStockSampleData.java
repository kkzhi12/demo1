package com.example.demo.service;

import com.example.demo.entity.BankStock;

import java.util.Arrays;
import java.util.List;

/**
 * 银行股示例数据
 * 基于2024-2025年A股主要银行股公开数据（近似值，仅供模型测试）
 *
 * @author : wangyuzhi
 */
public class BankStockSampleData {

    /**
     * 获取A股主要银行股示例数据
     * 数据来源：各银行2024年年报/2025年一季报（近似值）
     */
    public static List<BankStock> getSampleData() {
        return Arrays.asList(
                BankStock.builder()
                        .stockCode("601398").stockName("工商银行").currentPrice(6.5)
                        .roe(11.5).nim(1.61).costIncomeRatio(28.5)
                        .nplRatio(1.35).provisionCoverageRatio(214.0).specialMentionLoanRatio(1.8)
                        .revenueGrowthRate(1.2).netProfitGrowthRate(0.8).loanGrowthRate(10.5)
                        .pb(0.65).pe(5.8).dividendYield(5.5)
                        .coreCapitalAdequacyRatio(14.0).capitalAdequacyRatio(19.2)
                        .build(),

                BankStock.builder()
                        .stockCode("601288").stockName("农业银行").currentPrice(5.0)
                        .roe(11.8).nim(1.60).costIncomeRatio(30.2)
                        .nplRatio(1.32).provisionCoverageRatio(303.0).specialMentionLoanRatio(1.6)
                        .revenueGrowthRate(0.5).netProfitGrowthRate(4.7).loanGrowthRate(12.0)
                        .pb(0.62).pe(5.3).dividendYield(5.8)
                        .coreCapitalAdequacyRatio(11.3).capitalAdequacyRatio(18.2)
                        .build(),

                BankStock.builder()
                        .stockCode("601988").stockName("中国银行").currentPrice(5.2)
                        .roe(10.8).nim(1.44).costIncomeRatio(29.8)
                        .nplRatio(1.27).provisionCoverageRatio(201.0).specialMentionLoanRatio(2.0)
                        .revenueGrowthRate(-2.3).netProfitGrowthRate(2.6).loanGrowthRate(11.0)
                        .pb(0.58).pe(5.5).dividendYield(5.9)
                        .coreCapitalAdequacyRatio(12.2).capitalAdequacyRatio(18.7)
                        .build(),

                BankStock.builder()
                        .stockCode("601939").stockName("建设银行").currentPrice(8.2)
                        .roe(12.3).nim(1.70).costIncomeRatio(27.3)
                        .nplRatio(1.35).provisionCoverageRatio(239.0).specialMentionLoanRatio(1.9)
                        .revenueGrowthRate(-0.8).netProfitGrowthRate(2.9).loanGrowthRate(11.5)
                        .pb(0.68).pe(5.6).dividendYield(5.3)
                        .coreCapitalAdequacyRatio(14.1).capitalAdequacyRatio(19.5)
                        .build(),

                BankStock.builder()
                        .stockCode("601328").stockName("交通银行").currentPrice(7.5)
                        .roe(10.1).nim(1.28).costIncomeRatio(31.5)
                        .nplRatio(1.32).provisionCoverageRatio(195.0).specialMentionLoanRatio(2.2)
                        .revenueGrowthRate(-3.5).netProfitGrowthRate(0.9).loanGrowthRate(8.5)
                        .pb(0.55).pe(5.2).dividendYield(6.2)
                        .coreCapitalAdequacyRatio(10.7).capitalAdequacyRatio(16.3)
                        .build(),

                BankStock.builder()
                        .stockCode("600036").stockName("招商银行").currentPrice(38.0)
                        .roe(15.8).nim(2.00).costIncomeRatio(33.2)
                        .nplRatio(0.95).provisionCoverageRatio(437.0).specialMentionLoanRatio(1.1)
                        .revenueGrowthRate(-1.6).netProfitGrowthRate(1.2).loanGrowthRate(6.2)
                        .pb(1.05).pe(7.2).dividendYield(4.2)
                        .coreCapitalAdequacyRatio(14.2).capitalAdequacyRatio(17.9)
                        .build(),

                BankStock.builder()
                        .stockCode("601166").stockName("兴业银行").currentPrice(22.0)
                        .roe(11.0).nim(1.88).costIncomeRatio(28.8)
                        .nplRatio(1.07).provisionCoverageRatio(245.0).specialMentionLoanRatio(1.5)
                        .revenueGrowthRate(-3.8).netProfitGrowthRate(0.1).loanGrowthRate(5.8)
                        .pb(0.55).pe(5.0).dividendYield(5.8)
                        .coreCapitalAdequacyRatio(10.2).capitalAdequacyRatio(14.8)
                        .build(),

                BankStock.builder()
                        .stockCode("000001").stockName("平安银行").currentPrice(12.5)
                        .roe(10.5).nim(2.01).costIncomeRatio(35.0)
                        .nplRatio(1.06).provisionCoverageRatio(277.0).specialMentionLoanRatio(1.7)
                        .revenueGrowthRate(-8.5).netProfitGrowthRate(2.1).loanGrowthRate(2.3)
                        .pb(0.62).pe(5.8).dividendYield(3.5)
                        .coreCapitalAdequacyRatio(9.6).capitalAdequacyRatio(13.4)
                        .build(),

                BankStock.builder()
                        .stockCode("002142").stockName("宁波银行").currentPrice(25.0)
                        .roe(15.2).nim(1.87).costIncomeRatio(34.5)
                        .nplRatio(0.76).provisionCoverageRatio(461.0).specialMentionLoanRatio(0.8)
                        .revenueGrowthRate(6.8).netProfitGrowthRate(6.2).loanGrowthRate(18.5)
                        .pb(0.95).pe(6.5).dividendYield(3.0)
                        .coreCapitalAdequacyRatio(9.9).capitalAdequacyRatio(15.2)
                        .build(),

                BankStock.builder()
                        .stockCode("600926").stockName("杭州银行").currentPrice(15.0)
                        .roe(16.5).nim(1.57).costIncomeRatio(26.8)
                        .nplRatio(0.76).provisionCoverageRatio(543.0).specialMentionLoanRatio(0.6)
                        .revenueGrowthRate(5.2).netProfitGrowthRate(18.2).loanGrowthRate(20.3)
                        .pb(1.10).pe(6.8).dividendYield(3.2)
                        .coreCapitalAdequacyRatio(8.8).capitalAdequacyRatio(13.9)
                        .build(),

                BankStock.builder()
                        .stockCode("601009").stockName("南京银行").currentPrice(11.0)
                        .roe(14.8).nim(1.82).costIncomeRatio(27.5)
                        .nplRatio(0.83).provisionCoverageRatio(395.0).specialMentionLoanRatio(1.0)
                        .revenueGrowthRate(3.5).netProfitGrowthRate(9.5).loanGrowthRate(15.8)
                        .pb(0.72).pe(5.0).dividendYield(5.0)
                        .coreCapitalAdequacyRatio(9.5).capitalAdequacyRatio(14.0)
                        .build(),

                BankStock.builder()
                        .stockCode("601838").stockName("成都银行").currentPrice(18.0)
                        .roe(18.5).nim(1.80).costIncomeRatio(24.5)
                        .nplRatio(0.68).provisionCoverageRatio(502.0).specialMentionLoanRatio(0.5)
                        .revenueGrowthRate(8.5).netProfitGrowthRate(11.2).loanGrowthRate(22.0)
                        .pb(1.15).pe(6.2).dividendYield(3.8)
                        .coreCapitalAdequacyRatio(9.0).capitalAdequacyRatio(14.5)
                        .build(),

                BankStock.builder()
                        .stockCode("601128").stockName("常熟银行").currentPrice(8.5)
                        .roe(14.0).nim(2.85).costIncomeRatio(36.0)
                        .nplRatio(0.75).provisionCoverageRatio(536.0).specialMentionLoanRatio(0.9)
                        .revenueGrowthRate(10.2).netProfitGrowthRate(19.8).loanGrowthRate(16.5)
                        .pb(1.20).pe(8.5).dividendYield(2.5)
                        .coreCapitalAdequacyRatio(10.5).capitalAdequacyRatio(14.2)
                        .build(),

                BankStock.builder()
                        .stockCode("601658").stockName("邮储银行").currentPrice(5.8)
                        .roe(11.2).nim(1.91).costIncomeRatio(63.5)
                        .nplRatio(0.83).provisionCoverageRatio(325.0).specialMentionLoanRatio(1.2)
                        .revenueGrowthRate(2.0).netProfitGrowthRate(1.3).loanGrowthRate(12.8)
                        .pb(0.72).pe(6.5).dividendYield(4.5)
                        .coreCapitalAdequacyRatio(9.4).capitalAdequacyRatio(14.6)
                        .build()
        );
    }

}
