package com.example.demo.rest;

import com.example.demo.entity.StockFinanceData;
import com.example.demo.repository.StockFinanceDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 股票财务数据查看接口
 * 提供分页查询、历史数据等功能
 */
@RestController
@RequestMapping("/api/v1/stock-finance-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockFinanceDataController {

    private final StockFinanceDataRepository stockFinanceDataRepository;

    /**
     * 分页查询股票财务数据（每只股票只返回最新一期）
     *
     * @param stockCode      股票代码或名称（模糊查询，可选）
     * @param minRevenueYoy  营收同比最小值（可选）
     * @param minNetProfitYoy 净利润同比最小值（可选）
     * @param minRoe         ROE最小值（可选）
     * @param page           页码（从1开始）
     * @param size           每页条数
     */
    @GetMapping("/page")
    public Page<StockFinanceData> pageQuery(
            @RequestParam(value = "stockCode", required = false, defaultValue = "") String stockCode,
            @RequestParam(value = "minRevenueYoy", required = false) BigDecimal minRevenueYoy,
            @RequestParam(value = "minNetProfitYoy", required = false) BigDecimal minNetProfitYoy,
            @RequestParam(value = "minRoe", required = false) BigDecimal minRoe,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), size);
        return stockFinanceDataRepository.searchLatestByCondition(
                stockCode, minRevenueYoy, minNetProfitYoy, minRoe, pageRequest);
    }

    /**
     * 获取所有可用的报告期列表
     */
    @GetMapping("/report-periods")
    public List<String> getReportPeriods() {
        return stockFinanceDataRepository.findAllReportPeriods();
    }

    /**
     * 查询指定股票的历史数据
     */
    @GetMapping("/history/{stockCode}")
    public List<StockFinanceData> getHistory(@PathVariable String stockCode) {
        return stockFinanceDataRepository.findByStockCodeOrderByReportPeriodDesc(stockCode);
    }
}
