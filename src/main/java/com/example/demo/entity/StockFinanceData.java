package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_finance_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stock_code", "report_period"}))
public class StockFinanceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", length = 10, nullable = false)
    private String stockCode;

    @Column(name = "stock_name", length = 50, nullable = false)
    private String stockName;

    @Column(name = "report_period", length = 10, nullable = false)
    private String reportPeriod;

    @Column(name = "report_type", length = 20)
    private String reportType;

    // 每股指标
    @Column(name = "eps_jb", precision = 12, scale = 4)
    private BigDecimal epsJb; // 基本每股收益

    @Column(name = "eps_kc", precision = 12, scale = 4)
    private BigDecimal epsKc; // 扣非每股收益

    @Column(name = "eps_xs", precision = 12, scale = 4)
    private BigDecimal epsXs; // 稀释每股收益

    @Column(name = "bps", precision = 12, scale = 4)
    private BigDecimal bps; // 每股净资产

    @Column(name = "mgzbgj", precision = 12, scale = 4)
    private BigDecimal mgzbgj; // 每股资本公积

    @Column(name = "mgwfplr", precision = 12, scale = 4)
    private BigDecimal mgwfplr; // 每股未分配利润

    @Column(name = "mgjyxjje", precision = 12, scale = 4)
    private BigDecimal mgjyxjje; // 每股经营现金流

    // 营收利润
    @Column(name = "total_revenue", precision = 20, scale = 2)
    private BigDecimal totalRevenue; // 营业总收入

    @Column(name = "gross_profit", precision = 20, scale = 2)
    private BigDecimal grossProfit; // 毛利润

    @Column(name = "net_profit", precision = 20, scale = 2)
    private BigDecimal netProfit; // 归母净利润

    @Column(name = "net_profit_kc", precision = 20, scale = 2)
    private BigDecimal netProfitKc; // 扣非净利润

    // 增长率
    @Column(name = "revenue_yoy", precision = 14, scale = 4)
    private BigDecimal revenueYoy; // 营收同比增长率

    @Column(name = "net_profit_yoy", precision = 14, scale = 4)
    private BigDecimal netProfitYoy; // 净利润同比增长率

    @Column(name = "net_profit_kc_yoy", precision = 14, scale = 4)
    private BigDecimal netProfitKcYoy; // 扣非净利润同比增长率

    @Column(name = "revenue_qoq", precision = 14, scale = 4)
    private BigDecimal revenueQoq; // 营收环比增长率

    @Column(name = "net_profit_qoq", precision = 14, scale = 4)
    private BigDecimal netProfitQoq; // 净利润环比增长率

    @Column(name = "net_profit_kc_qoq", precision = 14, scale = 4)
    private BigDecimal netProfitKcQoq; // 扣非净利润环比增长率

    // 盈利能力
    @Column(name = "roe", precision = 14, scale = 4)
    private BigDecimal roe; // ROE加权

    @Column(name = "roe_kc", precision = 14, scale = 4)
    private BigDecimal roeKc; // ROE扣非加权

    @Column(name = "roa", precision = 14, scale = 4)
    private BigDecimal roa; // 总资产收益率

    @Column(name = "net_profit_margin", precision = 14, scale = 4)
    private BigDecimal netProfitMargin; // 销售净利率

    @Column(name = "gross_profit_margin", precision = 14, scale = 4)
    private BigDecimal grossProfitMargin; // 销售毛利率

    // 运营能力
    @Column(name = "cash_flow_ratio", precision = 14, scale = 4)
    private BigDecimal cashFlowRatio; // 经营现金流/营收

    @Column(name = "tax_rate", precision = 14, scale = 4)
    private BigDecimal taxRate; // 实际税率

    // 偿债能力
    @Column(name = "current_ratio", precision = 14, scale = 4)
    private BigDecimal currentRatio; // 流动比率

    @Column(name = "quick_ratio", precision = 14, scale = 4)
    private BigDecimal quickRatio; // 速动比率

    @Column(name = "cash_ratio", precision = 14, scale = 4)
    private BigDecimal cashRatioVal; // 现金比率

    @Column(name = "debt_ratio", precision = 14, scale = 4)
    private BigDecimal debtRatio; // 资产负债率

    @Column(name = "equity_multiplier", precision = 14, scale = 4)
    private BigDecimal equityMultiplier; // 权益乘数

    // 周转能力
    @Column(name = "total_asset_turnover", precision = 14, scale = 4)
    private BigDecimal totalAssetTurnover; // 总资产周转天数

    @Column(name = "inventory_turnover", precision = 14, scale = 4)
    private BigDecimal inventoryTurnover; // 存货周转天数

    @Column(name = "receivable_turnover", precision = 14, scale = 4)
    private BigDecimal receivableTurnover; // 应收账款周转天数

    // 元数据
    @Column(name = "import_time")
    private LocalDateTime importTime;

    @Column(name = "data_source", length = 50)
    private String dataSource;
}
