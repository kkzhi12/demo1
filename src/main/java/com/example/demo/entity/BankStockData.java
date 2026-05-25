package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行股财务数据实体（持久化到数据库）
 * 存储每只银行股每个报告期的核心指标
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bank_stock_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stock_code", "report_period"}))
public class BankStockData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 股票代码 */
    @Column(name = "stock_code", length = 10, nullable = false)
    private String stockCode;

    /** 股票名称 */
    @Column(name = "stock_name", length = 50, nullable = false)
    private String stockName;

    /** 报告期，如 2024Q4、2025Q1 */
    @Column(name = "report_period", length = 10, nullable = false)
    private String reportPeriod;

    // ========== 盈利能力指标 ==========

    /** ROE (%) */
    @Column(precision = 8, scale = 4)
    private BigDecimal roe;

    /** 净息差 (%) */
    @Column(precision = 8, scale = 4)
    private BigDecimal nim;

    /** 成本收入比 (%) */
    @Column(name = "cost_income_ratio", precision = 8, scale = 4)
    private BigDecimal costIncomeRatio;

    // ========== 资产质量指标 ==========

    /** 不良贷款率 (%) */
    @Column(name = "npl_ratio", precision = 8, scale = 4)
    private BigDecimal nplRatio;

    /** 拨备覆盖率 (%) */
    @Column(name = "provision_coverage_ratio", precision = 10, scale = 4)
    private BigDecimal provisionCoverageRatio;

    /** 关注类贷款占比 (%) */
    @Column(name = "special_mention_loan_ratio", precision = 8, scale = 4)
    private BigDecimal specialMentionLoanRatio;

    // ========== 成长性指标 ==========

    /** 营收增长率 (%) */
    @Column(name = "revenue_growth_rate", precision = 8, scale = 4)
    private BigDecimal revenueGrowthRate;

    /** 净利润增长率 (%) */
    @Column(name = "net_profit_growth_rate", precision = 8, scale = 4)
    private BigDecimal netProfitGrowthRate;

    /** 贷款余额增长率 (%) */
    @Column(name = "loan_growth_rate", precision = 8, scale = 4)
    private BigDecimal loanGrowthRate;

    // ========== 估值指标 ==========

    /** 市净率 PB */
    @Column(precision = 8, scale = 4)
    private BigDecimal pb;

    /** 市盈率 PE */
    @Column(precision = 8, scale = 4)
    private BigDecimal pe;

    /** 股息率 (%) */
    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;

    // ========== 资本充足性指标 ==========

    /** 核心一级资本充足率 (%) */
    @Column(name = "core_capital_adequacy_ratio", precision = 8, scale = 4)
    private BigDecimal coreCapitalAdequacyRatio;

    /** 资本充足率 (%) */
    @Column(name = "capital_adequacy_ratio", precision = 8, scale = 4)
    private BigDecimal capitalAdequacyRatio;

    // ========== 元数据 ==========

    /** 导入时间 */
    @Column(name = "import_time")
    private LocalDateTime importTime;

    /** 数据来源 */
    @Column(name = "data_source", length = 50)
    private String dataSource;
}
