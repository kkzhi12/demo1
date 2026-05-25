-- 银行股财务数据表
CREATE TABLE IF NOT EXISTS bank_stock_data (
    id BIGSERIAL PRIMARY KEY,
    stock_code VARCHAR(10) NOT NULL,
    stock_name VARCHAR(50) NOT NULL,
    report_period VARCHAR(10) NOT NULL,
    roe DECIMAL(8,4),
    nim DECIMAL(8,4),
    cost_income_ratio DECIMAL(8,4),
    npl_ratio DECIMAL(8,4),
    provision_coverage_ratio DECIMAL(10,4),
    special_mention_loan_ratio DECIMAL(8,4),
    revenue_growth_rate DECIMAL(8,4),
    net_profit_growth_rate DECIMAL(8,4),
    loan_growth_rate DECIMAL(8,4),
    pb DECIMAL(8,4),
    pe DECIMAL(8,4),
    dividend_yield DECIMAL(8,4),
    core_capital_adequacy_ratio DECIMAL(8,4),
    capital_adequacy_ratio DECIMAL(8,4),
    import_time TIMESTAMP,
    data_source VARCHAR(50),
    CONSTRAINT uk_stock_period UNIQUE (stock_code, report_period)
);

-- 分析结果表
CREATE TABLE IF NOT EXISTS bank_stock_analysis (
    id BIGSERIAL PRIMARY KEY,
    analysis_batch VARCHAR(50),
    stock_code VARCHAR(10),
    stock_name VARCHAR(50),
    profitability_score DECIMAL(6,2),
    asset_quality_score DECIMAL(6,2),
    growth_score DECIMAL(6,2),
    valuation_score DECIMAL(6,2),
    capital_score DECIMAL(6,2),
    total_score DECIMAL(6,2),
    rank INTEGER,
    analysis_time TIMESTAMP
);

-- 权重配置表
CREATE TABLE IF NOT EXISTS model_weight_config (
    id BIGSERIAL PRIMARY KEY,
    strategy_name VARCHAR(50) NOT NULL,
    profitability_weight DECIMAL(4,2),
    asset_quality_weight DECIMAL(4,2),
    growth_weight DECIMAL(4,2),
    valuation_weight DECIMAL(4,2),
    capital_weight DECIMAL(4,2),
    is_default BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP
);

-- 插入默认权重策略
INSERT INTO model_weight_config (strategy_name, profitability_weight, asset_quality_weight, growth_weight, valuation_weight, capital_weight, is_default, create_time)
VALUES
('默认均衡', 0.25, 0.30, 0.20, 0.15, 0.10, TRUE, NOW()),
('高股息策略', 0.20, 0.25, 0.10, 0.35, 0.10, FALSE, NOW()),
('成长性策略', 0.20, 0.20, 0.35, 0.15, 0.10, FALSE, NOW()),
('安全性策略', 0.15, 0.40, 0.10, 0.15, 0.20, FALSE, NOW());
