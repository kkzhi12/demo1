package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 银行股评分结果实体
 * 包含各维度得分和综合得分
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStockScore {

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 当前股价 */
    private Double currentPrice;

    // ========== 各维度得分 (0-100) ==========

    /** 盈利能力得分 */
    private Double profitabilityScore;

    /** 资产质量得分 */
    private Double assetQualityScore;

    /** 成长性得分 */
    private Double growthScore;

    /** 估值/安全边际得分 */
    private Double valuationScore;

    /** 资本充足性得分 */
    private Double capitalScore;

    // ========== 综合得分 ==========

    /** 综合得分 (0-100) */
    private Double totalScore;

    /** 排名 */
    private Integer rank;

    // ========== 各维度明细（用于展示） ==========

    /** ROE原始值 */
    private Double roe;

    /** 净息差原始值 */
    private Double nim;

    /** 不良贷款率原始值 */
    private Double nplRatio;

    /** 拨备覆盖率原始值 */
    private Double provisionCoverageRatio;

    /** 净利润增长率原始值 */
    private Double netProfitGrowthRate;

    /** PB原始值 */
    private Double pb;

    /** 股息率原始值 */
    private Double dividendYield;

    /** 核心一级资本充足率原始值 */
    private Double coreCapitalAdequacyRatio;

}
