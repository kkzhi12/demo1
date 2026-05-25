package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型权重配置
 * 可以通过接口动态调整各维度权重
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelWeight {

    // ========== 五大维度权重（总和为1.0） ==========

    /** 盈利能力权重，默认0.25 */
    @Builder.Default
    private Double profitabilityWeight = 0.25;

    /** 资产质量权重，默认0.30 */
    @Builder.Default
    private Double assetQualityWeight = 0.30;

    /** 成长性权重，默认0.20 */
    @Builder.Default
    private Double growthWeight = 0.20;

    /** 估值/安全边际权重，默认0.15 */
    @Builder.Default
    private Double valuationWeight = 0.15;

    /** 资本充足性权重，默认0.10 */
    @Builder.Default
    private Double capitalWeight = 0.10;

    // ========== 盈利能力内部权重 ==========

    /** ROE权重 */
    @Builder.Default
    private Double roeWeight = 0.50;

    /** 净息差权重 */
    @Builder.Default
    private Double nimWeight = 0.30;

    /** 成本收入比权重 */
    @Builder.Default
    private Double costIncomeRatioWeight = 0.20;

    // ========== 资产质量内部权重 ==========

    /** 不良贷款率权重 */
    @Builder.Default
    private Double nplRatioWeight = 0.40;

    /** 拨备覆盖率权重 */
    @Builder.Default
    private Double provisionCoverageWeight = 0.35;

    /** 关注类贷款占比权重 */
    @Builder.Default
    private Double specialMentionWeight = 0.25;

    // ========== 成长性内部权重 ==========

    /** 营收增长率权重 */
    @Builder.Default
    private Double revenueGrowthWeight = 0.30;

    /** 净利润增长率权重 */
    @Builder.Default
    private Double netProfitGrowthWeight = 0.45;

    /** 贷款余额增长率权重 */
    @Builder.Default
    private Double loanGrowthWeight = 0.25;

    // ========== 估值内部权重 ==========

    /** PB权重 */
    @Builder.Default
    private Double pbWeight = 0.40;

    /** PE权重 */
    @Builder.Default
    private Double peWeight = 0.25;

    /** 股息率权重 */
    @Builder.Default
    private Double dividendYieldWeight = 0.35;

    // ========== 资本充足性内部权重 ==========

    /** 核心一级资本充足率权重 */
    @Builder.Default
    private Double coreCapitalWeight = 0.60;

    /** 资本充足率权重 */
    @Builder.Default
    private Double totalCapitalWeight = 0.40;

    // ========== 动态调整因子 ==========

    /** 趋势调整系数 alpha，默认0.15 */
    @Builder.Default
    private Double trendAlpha = 0.15;

    /**
     * 获取默认权重配置
     */
    public static ModelWeight defaultWeight() {
        return ModelWeight.builder().build();
    }

}
