package com.example.demo.service;

import cn.idev.excel.FastExcel;
import com.example.demo.entity.BankStock;
import com.example.demo.entity.BankStockData;
import com.example.demo.repository.BankStockDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 银行股数据导入服务
 * 支持从Excel批量导入银行财务数据到数据库
 *
 * @author : wangyuzhi
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BankStockDataService {

    private final BankStockDataRepository bankStockDataRepository;

    /**
     * 从Excel导入银行股数据到数据库
     *
     * @param file         Excel文件
     * @param reportPeriod 报告期（如 2024Q4）
     * @param dataSource   数据来源（如 东方财富导出）
     * @return 导入结果信息
     */
    @Transactional
    public ImportResult importFromExcel(MultipartFile file, String reportPeriod, String dataSource) {
        try {
            BaseExcelListener<BankStock> listener = new BaseExcelListener<>();
            FastExcel.read(file.getInputStream(), BankStock.class, listener).sheet(0).doRead();
            List<BankStock> bankStocks = listener.getDataList();

            if (bankStocks == null || bankStocks.isEmpty()) {
                return ImportResult.fail("Excel中无有效数据");
            }

            int insertCount = 0;
            int updateCount = 0;

            for (BankStock stock : bankStocks) {
                if (stock.getStockCode() == null || stock.getStockCode().isBlank()) {
                    continue;
                }

                Optional<BankStockData> existing = bankStockDataRepository
                        .findByStockCodeAndReportPeriod(stock.getStockCode(), reportPeriod);

                BankStockData data;
                if (existing.isPresent()) {
                    data = existing.get();
                    updateCount++;
                } else {
                    data = new BankStockData();
                    data.setStockCode(stock.getStockCode());
                    data.setReportPeriod(reportPeriod);
                    insertCount++;
                }

                // 映射字段
                data.setStockName(stock.getStockName());
                data.setRoe(toBigDecimal(stock.getRoe()));
                data.setNim(toBigDecimal(stock.getNim()));
                data.setCostIncomeRatio(toBigDecimal(stock.getCostIncomeRatio()));
                data.setNplRatio(toBigDecimal(stock.getNplRatio()));
                data.setProvisionCoverageRatio(toBigDecimal(stock.getProvisionCoverageRatio()));
                data.setSpecialMentionLoanRatio(toBigDecimal(stock.getSpecialMentionLoanRatio()));
                data.setRevenueGrowthRate(toBigDecimal(stock.getRevenueGrowthRate()));
                data.setNetProfitGrowthRate(toBigDecimal(stock.getNetProfitGrowthRate()));
                data.setLoanGrowthRate(toBigDecimal(stock.getLoanGrowthRate()));
                data.setPb(toBigDecimal(stock.getPb()));
                data.setPe(toBigDecimal(stock.getPe()));
                data.setDividendYield(toBigDecimal(stock.getDividendYield()));
                data.setCoreCapitalAdequacyRatio(toBigDecimal(stock.getCoreCapitalAdequacyRatio()));
                data.setCapitalAdequacyRatio(toBigDecimal(stock.getCapitalAdequacyRatio()));
                data.setImportTime(LocalDateTime.now());
                data.setDataSource(dataSource);

                bankStockDataRepository.save(data);
            }

            log.info("Excel导入完成：新增{}条，更新{}条，报告期={}", insertCount, updateCount, reportPeriod);
            return ImportResult.success(insertCount, updateCount);

        } catch (Exception e) {
            log.error("Excel导入失败", e);
            return ImportResult.fail("导入失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定报告期的所有银行数据
     */
    public List<BankStockData> findByReportPeriod(String reportPeriod) {
        return bankStockDataRepository.findByReportPeriod(reportPeriod);
    }

    /**
     * 查询某只银行股的历史数据
     */
    public List<BankStockData> findByStockCode(String stockCode) {
        return bankStockDataRepository.findByStockCodeOrderByReportPeriodDesc(stockCode);
    }

    /**
     * 查询所有可用的报告期
     */
    public List<String> findAllReportPeriods() {
        return bankStockDataRepository.findAllReportPeriods();
    }

    /**
     * 查询所有数据
     */
    public List<BankStockData> findAll() {
        return bankStockDataRepository.findAll();
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    /**
     * 导入结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportResult {
        private boolean success;
        private String message;
        private int insertCount;
        private int updateCount;

        public static ImportResult success(int insertCount, int updateCount) {
            return new ImportResult(true,
                    String.format("导入成功：新增%d条，更新%d条", insertCount, updateCount),
                    insertCount, updateCount);
        }

        public static ImportResult fail(String message) {
            return new ImportResult(false, message, 0, 0);
        }
    }
}
