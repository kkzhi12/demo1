package com.example.demo.entity;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 银行股基础数据实体
 * 从Excel导入的原始数据，包含五个维度的核心指标
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStock {

    // ========== 基本信息 ==========

    @ExcelProperty("股票代码")
    private String stockCode;

    @ExcelProperty("股票名称")
    private String stockName;

    @ExcelProperty("当前股价")
    private Double currentPrice;

    // ========== 盈利能力指标 ==========

    /** 净资产收益率 ROE (%) */
    @ExcelProperty("ROE")
    private Double roe;

    /** 净息差 NIM (%) */
    @ExcelProperty("净息差")
    private Double nim;

    /** 成本收入比 (%) - 越低越好 */
    @ExcelProperty("成本收入比")
    private Double costIncomeRatio;

    // ========== 资产质量指标 ==========

    /** 不良贷款率 (%) - 越低越好 */
    @ExcelProperty("不良贷款率")
    private Double nplRatio;

    /** 拨备覆盖率 (%) */
    @ExcelProperty("拨备覆盖率")
    private Double provisionCoverageRatio;

    /** 关注类贷款占比 (%) - 越低越好 */
    @ExcelProperty("关注类贷款占比")
    private Double specialMentionLoanRatio;

    // ========== 成长性指标 ==========

    /** 营收增长率 (%) */
    @ExcelProperty("营收增长率")
    private Double revenueGrowthRate;

    /** 净利润增长率 (%) */
    @ExcelProperty("净利润增长率")
    private Double netProfitGrowthRate;

    /** 贷款余额增长率 (%) */
    @ExcelProperty("贷款余额增长率")
    private Double loanGrowthRate;

    // ========== 估值/安全边际指标 ==========

    /** 市净率 PB */
    @ExcelProperty("PB")
    private Double pb;

    /** 市盈率 PE */
    @ExcelProperty("PE")
    private Double pe;

    /** 股息率 (%) */
    @ExcelProperty("股息率")
    private Double dividendYield;

    // ========== 资本充足性指标 ==========

    /** 核心一级资本充足率 (%) */
    @ExcelProperty("核心一级资本充足率")
    private Double coreCapitalAdequacyRatio;

    /** 资本充足率 (%) */
    @ExcelProperty("资本充足率")
    private Double capitalAdequacyRatio;

}
