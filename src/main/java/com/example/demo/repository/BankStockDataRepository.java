package com.example.demo.repository;

import com.example.demo.entity.BankStockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 银行股财务数据 Repository
 *
 * @author : wangyuzhi
 */
@Repository
public interface BankStockDataRepository extends JpaRepository<BankStockData, Long> {

    /** 根据报告期查询所有银行数据 */
    List<BankStockData> findByReportPeriod(String reportPeriod);

    /** 根据股票代码查询所有报告期数据 */
    List<BankStockData> findByStockCodeOrderByReportPeriodDesc(String stockCode);

    /** 根据股票代码和报告期查询（唯一） */
    Optional<BankStockData> findByStockCodeAndReportPeriod(String stockCode, String reportPeriod);

    /** 查询所有可用的报告期 */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT d.reportPeriod FROM BankStockData d ORDER BY d.reportPeriod DESC")
    List<String> findAllReportPeriods();
}
