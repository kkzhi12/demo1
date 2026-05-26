package com.example.demo.rest;

import com.example.demo.entity.BankStockData;
import com.example.demo.repository.BankStockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 银行股数据查看接口
 * 提供分页查询、条件筛选等功能
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/bank-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BankStockDataController {

    private final BankStockDataRepository bankStockDataRepository;

    /**
     * 分页查询银行股数据（每只股票只返回最新一期）
     *
     * @param stockCode 股票代码或名称（模糊查询，可选）
     * @param page      页码（从1开始）
     * @param size      每页条数
     */
    @GetMapping("/page")
    public Page<BankStockData> pageQuery(
            @RequestParam(value = "stockCode", required = false, defaultValue = "") String stockCode,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), size);
        return bankStockDataRepository.searchLatestByCondition(stockCode, pageRequest);
    }

    /**
     * 获取所有可用的报告期列表（用于下拉框）
     */
    @GetMapping("/report-periods")
    public List<String> getReportPeriods() {
        return bankStockDataRepository.findAllReportPeriods();
    }

    /**
     * 获取最新报告期
     */
    @GetMapping("/latest-period")
    public String getLatestPeriod() {
        String period = bankStockDataRepository.findLatestReportPeriod();
        return period != null ? period : "";
    }

    /**
     * 查询指定股票的历史数据
     */
    @GetMapping("/history/{stockCode}")
    public List<BankStockData> getHistory(@PathVariable String stockCode) {
        return bankStockDataRepository.findByStockCodeOrderByReportPeriodDesc(stockCode);
    }
}
