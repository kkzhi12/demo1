package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行股分析结果实体（持久化到数据库）
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bank_stock_analysis")
public class BankStockAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分析批次号 */
    @Column(name = "analysis_batch", length = 50)
    private String analysisBatch;

    /** 股票代码 */
    @Column(name = "stock_code", length = 10)
    private String stockCode;

    /** 股票名称 */
    @Column(name = "stock_name", length = 50)
    private String stockName;

    /** 盈利能力得分 */
    @Column(name = "profitability_score", precision = 6, scale = 2)
    private BigDecimal profitabilityScore;

    /** 资产质量得分 */
    @Column(name = "asset_quality_score", precision = 6, scale = 2)
    private BigDecimal assetQualityScore;

    /** 成长性得分 */
    @Column(name = "growth_score", precision = 6, scale = 2)
    private BigDecimal growthScore;

    /** 估值得分 */
    @Column(name = "valuation_score", precision = 6, scale = 2)
    private BigDecimal valuationScore;

    /** 资本充足得分 */
    @Column(name = "capital_score", precision = 6, scale = 2)
    private BigDecimal capitalScore;

    /** 综合得分 */
    @Column(name = "total_score", precision = 6, scale = 2)
    private BigDecimal totalScore;

    /** 排名 */
    @Column(name = "rank")
    private Integer rank;

    /** 分析时间 */
    @Column(name = "analysis_time")
    private LocalDateTime analysisTime;
}
