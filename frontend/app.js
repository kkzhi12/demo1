// 后端API地址
const API_BASE = 'http://localhost:8080/api/v1/bank-stock';

// 示例数据（与后端BankStockSampleData一致）
const SAMPLE_DATA = [
    { stockCode: '601398', stockName: '工商银行', currentPrice: 6.5, roe: 11.5, nim: 1.61, costIncomeRatio: 28.5, nplRatio: 1.35, provisionCoverageRatio: 214, specialMentionLoanRatio: 1.8, revenueGrowthRate: 1.2, netProfitGrowthRate: 0.8, loanGrowthRate: 10.5, pb: 0.65, pe: 5.8, dividendYield: 5.5, coreCapitalAdequacyRatio: 14.0, capitalAdequacyRatio: 19.2 },
    { stockCode: '601288', stockName: '农业银行', currentPrice: 5.0, roe: 11.8, nim: 1.60, costIncomeRatio: 30.2, nplRatio: 1.32, provisionCoverageRatio: 303, specialMentionLoanRatio: 1.6, revenueGrowthRate: 0.5, netProfitGrowthRate: 4.7, loanGrowthRate: 12.0, pb: 0.62, pe: 5.3, dividendYield: 5.8, coreCapitalAdequacyRatio: 11.3, capitalAdequacyRatio: 18.2 },
    { stockCode: '601988', stockName: '中国银行', currentPrice: 5.2, roe: 10.8, nim: 1.44, costIncomeRatio: 29.8, nplRatio: 1.27, provisionCoverageRatio: 201, specialMentionLoanRatio: 2.0, revenueGrowthRate: -2.3, netProfitGrowthRate: 2.6, loanGrowthRate: 11.0, pb: 0.58, pe: 5.5, dividendYield: 5.9, coreCapitalAdequacyRatio: 12.2, capitalAdequacyRatio: 18.7 },
    { stockCode: '601939', stockName: '建设银行', currentPrice: 8.2, roe: 12.3, nim: 1.70, costIncomeRatio: 27.3, nplRatio: 1.35, provisionCoverageRatio: 239, specialMentionLoanRatio: 1.9, revenueGrowthRate: -0.8, netProfitGrowthRate: 2.9, loanGrowthRate: 11.5, pb: 0.68, pe: 5.6, dividendYield: 5.3, coreCapitalAdequacyRatio: 14.1, capitalAdequacyRatio: 19.5 },
    { stockCode: '601328', stockName: '交通银行', currentPrice: 7.5, roe: 10.1, nim: 1.28, costIncomeRatio: 31.5, nplRatio: 1.32, provisionCoverageRatio: 195, specialMentionLoanRatio: 2.2, revenueGrowthRate: -3.5, netProfitGrowthRate: 0.9, loanGrowthRate: 8.5, pb: 0.55, pe: 5.2, dividendYield: 6.2, coreCapitalAdequacyRatio: 10.7, capitalAdequacyRatio: 16.3 },
    { stockCode: '600036', stockName: '招商银行', currentPrice: 38.0, roe: 15.8, nim: 2.00, costIncomeRatio: 33.2, nplRatio: 0.95, provisionCoverageRatio: 437, specialMentionLoanRatio: 1.1, revenueGrowthRate: -1.6, netProfitGrowthRate: 1.2, loanGrowthRate: 6.2, pb: 1.05, pe: 7.2, dividendYield: 4.2, coreCapitalAdequacyRatio: 14.2, capitalAdequacyRatio: 17.9 },
    { stockCode: '601166', stockName: '兴业银行', currentPrice: 22.0, roe: 11.0, nim: 1.88, costIncomeRatio: 28.8, nplRatio: 1.07, provisionCoverageRatio: 245, specialMentionLoanRatio: 1.5, revenueGrowthRate: -3.8, netProfitGrowthRate: 0.1, loanGrowthRate: 5.8, pb: 0.55, pe: 5.0, dividendYield: 5.8, coreCapitalAdequacyRatio: 10.2, capitalAdequacyRatio: 14.8 },
    { stockCode: '000001', stockName: '平安银行', currentPrice: 12.5, roe: 10.5, nim: 2.01, costIncomeRatio: 35.0, nplRatio: 1.06, provisionCoverageRatio: 277, specialMentionLoanRatio: 1.7, revenueGrowthRate: -8.5, netProfitGrowthRate: 2.1, loanGrowthRate: 2.3, pb: 0.62, pe: 5.8, dividendYield: 3.5, coreCapitalAdequacyRatio: 9.6, capitalAdequacyRatio: 13.4 },
    { stockCode: '002142', stockName: '宁波银行', currentPrice: 25.0, roe: 15.2, nim: 1.87, costIncomeRatio: 34.5, nplRatio: 0.76, provisionCoverageRatio: 461, specialMentionLoanRatio: 0.8, revenueGrowthRate: 6.8, netProfitGrowthRate: 6.2, loanGrowthRate: 18.5, pb: 0.95, pe: 6.5, dividendYield: 3.0, coreCapitalAdequacyRatio: 9.9, capitalAdequacyRatio: 15.2 },
    { stockCode: '600926', stockName: '杭州银行', currentPrice: 15.0, roe: 16.5, nim: 1.57, costIncomeRatio: 26.8, nplRatio: 0.76, provisionCoverageRatio: 543, specialMentionLoanRatio: 0.6, revenueGrowthRate: 5.2, netProfitGrowthRate: 18.2, loanGrowthRate: 20.3, pb: 1.10, pe: 6.8, dividendYield: 3.2, coreCapitalAdequacyRatio: 8.8, capitalAdequacyRatio: 13.9 },
    { stockCode: '601009', stockName: '南京银行', currentPrice: 11.0, roe: 14.8, nim: 1.82, costIncomeRatio: 27.5, nplRatio: 0.83, provisionCoverageRatio: 395, specialMentionLoanRatio: 1.0, revenueGrowthRate: 3.5, netProfitGrowthRate: 9.5, loanGrowthRate: 15.8, pb: 0.72, pe: 5.0, dividendYield: 5.0, coreCapitalAdequacyRatio: 9.5, capitalAdequacyRatio: 14.0 },
    { stockCode: '601838', stockName: '成都银行', currentPrice: 18.0, roe: 18.5, nim: 1.80, costIncomeRatio: 24.5, nplRatio: 0.68, provisionCoverageRatio: 502, specialMentionLoanRatio: 0.5, revenueGrowthRate: 8.5, netProfitGrowthRate: 11.2, loanGrowthRate: 22.0, pb: 1.15, pe: 6.2, dividendYield: 3.8, coreCapitalAdequacyRatio: 9.0, capitalAdequacyRatio: 14.5 },
    { stockCode: '601128', stockName: '常熟银行', currentPrice: 8.5, roe: 14.0, nim: 2.85, costIncomeRatio: 36.0, nplRatio: 0.75, provisionCoverageRatio: 536, specialMentionLoanRatio: 0.9, revenueGrowthRate: 10.2, netProfitGrowthRate: 19.8, loanGrowthRate: 16.5, pb: 1.20, pe: 8.5, dividendYield: 2.5, coreCapitalAdequacyRatio: 10.5, capitalAdequacyRatio: 14.2 },
    { stockCode: '601658', stockName: '邮储银行', currentPrice: 5.8, roe: 11.2, nim: 1.91, costIncomeRatio: 63.5, nplRatio: 0.83, provisionCoverageRatio: 325, specialMentionLoanRatio: 1.2, revenueGrowthRate: 2.0, netProfitGrowthRate: 1.3, loanGrowthRate: 12.8, pb: 0.72, pe: 6.5, dividendYield: 4.5, coreCapitalAdequacyRatio: 9.4, capitalAdequacyRatio: 14.6 }
];

// 策略预设
const STRATEGIES = {
    default: { profitability: 0.25, assetQuality: 0.30, growth: 0.20, valuation: 0.15, capital: 0.10 },
    dividend: { profitability: 0.20, assetQuality: 0.25, growth: 0.10, valuation: 0.35, capital: 0.10 },
    growth: { profitability: 0.25, assetQuality: 0.20, growth: 0.35, valuation: 0.10, capital: 0.10 },
    safety: { profitability: 0.20, assetQuality: 0.35, growth: 0.15, valuation: 0.15, capital: 0.15 }
};

// 字段定义
const FIELDS = [
    'stockCode', 'stockName', 'currentPrice',
    'roe', 'nim', 'costIncomeRatio',
    'nplRatio', 'provisionCoverageRatio', 'specialMentionLoanRatio',
    'revenueGrowthRate', 'netProfitGrowthRate', 'loanGrowthRate',
    'pb', 'pe', 'dividendYield',
    'coreCapitalAdequacyRatio', 'capitalAdequacyRatio'
];

// 页面加载时添加两行空数据
document.addEventListener('DOMContentLoaded', () => {
    addRow();
    addRow();
});

/**
 * 添加一行数据
 */
function addRow(data = null) {
    const tbody = document.getElementById('data-body');
    const row = document.createElement('tr');

    // 删除按钮
    let html = '<td><button class="btn-delete" onclick="deleteRow(this)">删除</button></td>';

    FIELDS.forEach((field, index) => {
        const value = data ? (data[field] || '') : '';
        const type = index < 2 ? 'text' : 'number';
        const step = index < 2 ? '' : 'step="0.01"';
        const width = index < 2 ? 'width:80px' : 'width:65px';
        html += `<td><input type="${type}" ${step} style="${width}" data-field="${field}" value="${value}"></td>`;
    });

    row.innerHTML = html;
    tbody.appendChild(row);
}

/**
 * 删除一行
 */
function deleteRow(btn) {
    const row = btn.closest('tr');
    row.remove();
}

/**
 * 载入示例数据
 */
function loadSampleData() {
    clearAll();
    SAMPLE_DATA.forEach(data => addRow(data));
}

/**
 * 清空所有数据
 */
function clearAll() {
    document.getElementById('data-body').innerHTML = '';
}

/**
 * 应用策略预设
 */
function applyStrategy(name) {
    const s = STRATEGIES[name];
    document.getElementById('w-profitability').value = s.profitability;
    document.getElementById('w-assetQuality').value = s.assetQuality;
    document.getElementById('w-growth').value = s.growth;
    document.getElementById('w-valuation').value = s.valuation;
    document.getElementById('w-capital').value = s.capital;
}

/**
 * 收集表格数据
 */
function collectTableData() {
    const rows = document.querySelectorAll('#data-body tr');
    const bankStocks = [];

    rows.forEach(row => {
        const inputs = row.querySelectorAll('input');
        const stock = {};
        let hasData = false;

        inputs.forEach(input => {
            const field = input.dataset.field;
            const value = input.value.trim();
            if (value) {
                hasData = true;
                if (input.type === 'number') {
                    stock[field] = parseFloat(value);
                } else {
                    stock[field] = value;
                }
            }
        });

        if (hasData && stock.stockName) {
            bankStocks.push(stock);
        }
    });

    return bankStocks;
}

/**
 * 收集权重配置
 */
function collectWeights() {
    return {
        profitabilityWeight: parseFloat(document.getElementById('w-profitability').value),
        assetQualityWeight: parseFloat(document.getElementById('w-assetQuality').value),
        growthWeight: parseFloat(document.getElementById('w-growth').value),
        valuationWeight: parseFloat(document.getElementById('w-valuation').value),
        capitalWeight: parseFloat(document.getElementById('w-capital').value)
    };
}

/**
 * 运行分析
 */
async function runAnalysis() {
    const bankStocks = collectTableData();

    if (bankStocks.length < 2) {
        alert('至少需要输入2家银行的数据才能进行比较分析！');
        return;
    }

    const weight = collectWeights();
    const weightSum = weight.profitabilityWeight + weight.assetQualityWeight +
        weight.growthWeight + weight.valuationWeight + weight.capitalWeight;

    if (Math.abs(weightSum - 1.0) > 0.01) {
        alert(`权重总和应为1.0，当前为${weightSum.toFixed(2)}，请调整！`);
        return;
    }

    const requestBody = { bankStocks, weight };

    try {
        const response = await fetch(`${API_BASE}/analyze-json`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const results = await response.json();
        displayResults(results);
    } catch (error) {
        // 如果后端不可用，使用前端本地计算
        console.warn('后端不可用，使用前端本地计算:', error.message);
        const results = calculateLocally(bankStocks, weight);
        displayResults(results);
    }
}

/**
 * 前端本地计算（后端不可用时的备选方案）
 */
function calculateLocally(bankStocks, weight) {
    const n = bankStocks.length;

    // 百分位排名计算
    function percentileRank(arr, field, ascending) {
        const indexed = arr.map((item, i) => ({ index: i, value: item[field] || 0 }));
        if (ascending) {
            indexed.sort((a, b) => a.value - b.value);
        } else {
            indexed.sort((a, b) => b.value - a.value);
        }
        const scores = new Array(n);
        indexed.forEach((item, rank) => {
            scores[item.index] = n > 1 ? (rank / (n - 1)) * 100 : 50;
        });
        return scores;
    }

    // 计算各指标得分
    const scores = {
        roe: percentileRank(bankStocks, 'roe', true),
        nim: percentileRank(bankStocks, 'nim', true),
        costIncomeRatio: percentileRank(bankStocks, 'costIncomeRatio', false),
        nplRatio: percentileRank(bankStocks, 'nplRatio', false),
        provisionCoverage: percentileRank(bankStocks, 'provisionCoverageRatio', true),
        specialMention: percentileRank(bankStocks, 'specialMentionLoanRatio', false),
        revenueGrowth: percentileRank(bankStocks, 'revenueGrowthRate', true),
        netProfitGrowth: percentileRank(bankStocks, 'netProfitGrowthRate', true),
        loanGrowth: percentileRank(bankStocks, 'loanGrowthRate', true),
        pb: percentileRank(bankStocks, 'pb', false),
        pe: percentileRank(bankStocks, 'pe', false),
        dividendYield: percentileRank(bankStocks, 'dividendYield', true),
        coreCapital: percentileRank(bankStocks, 'coreCapitalAdequacyRatio', true),
        totalCapital: percentileRank(bankStocks, 'capitalAdequacyRatio', true)
    };

    // 计算各维度得分
    const results = bankStocks.map((stock, i) => {
        const profitabilityScore = 0.50 * scores.roe[i] + 0.30 * scores.nim[i] + 0.20 * scores.costIncomeRatio[i];
        const assetQualityScore = 0.40 * scores.nplRatio[i] + 0.35 * scores.provisionCoverage[i] + 0.25 * scores.specialMention[i];
        const growthScore = 0.30 * scores.revenueGrowth[i] + 0.45 * scores.netProfitGrowth[i] + 0.25 * scores.loanGrowth[i];
        const valuationScore = 0.40 * scores.pb[i] + 0.25 * scores.pe[i] + 0.35 * scores.dividendYield[i];
        const capitalScore = 0.60 * scores.coreCapital[i] + 0.40 * scores.totalCapital[i];

        const totalScore = weight.profitabilityWeight * profitabilityScore +
            weight.assetQualityWeight * assetQualityScore +
            weight.growthWeight * growthScore +
            weight.valuationWeight * valuationScore +
            weight.capitalWeight * capitalScore;

        return {
            stockCode: stock.stockCode,
            stockName: stock.stockName,
            currentPrice: stock.currentPrice,
            profitabilityScore: Math.round(profitabilityScore * 100) / 100,
            assetQualityScore: Math.round(assetQualityScore * 100) / 100,
            growthScore: Math.round(growthScore * 100) / 100,
            valuationScore: Math.round(valuationScore * 100) / 100,
            capitalScore: Math.round(capitalScore * 100) / 100,
            totalScore: Math.round(totalScore * 100) / 100,
            roe: stock.roe,
            pb: stock.pb,
            dividendYield: stock.dividendYield
        };
    });

    // 排序
    results.sort((a, b) => b.totalScore - a.totalScore);
    results.forEach((r, i) => r.rank = i + 1);

    return results;
}

/**
 * 展示分析结果
 */
function displayResults(results) {
    const section = document.getElementById('result-section');
    section.style.display = 'block';

    // 滚动到结果区域
    section.scrollIntoView({ behavior: 'smooth' });

    // Top 3 摘要卡片
    const summaryDiv = document.getElementById('result-summary');
    const medals = ['🥇', '🥈', '🥉'];
    summaryDiv.innerHTML = results.slice(0, 3).map((r, i) => `
        <div class="summary-card">
            <div class="rank">${medals[i]} 第${r.rank}名</div>
            <div class="name">${r.stockName} (${r.stockCode})</div>
            <div class="score">综合得分: ${r.totalScore} | 股息率: ${r.dividendYield || '-'}%</div>
        </div>
    `).join('');

    // 详细结果表格
    const tbody = document.getElementById('result-body');
    tbody.innerHTML = results.map(r => `
        <tr>
            <td class="rank-cell">${r.rank}</td>
            <td>${r.stockName}</td>
            <td>${r.stockCode}</td>
            <td class="score-cell">${r.totalScore}</td>
            <td>${r.profitabilityScore}</td>
            <td>${r.assetQualityScore}</td>
            <td>${r.growthScore}</td>
            <td>${r.valuationScore}</td>
            <td>${r.capitalScore}</td>
            <td>${r.roe || '-'}</td>
            <td>${r.pb || '-'}</td>
            <td>${r.dividendYield || '-'}</td>
        </tr>
    `).join('');
}
