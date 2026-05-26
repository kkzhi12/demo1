package com.example.demo.repository;

import com.example.demo.entity.BankStockData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT DISTINCT d.reportPeriod FROM BankStockData d ORDER BY d.reportPeriod DESC")
    List<String> findAllReportPeriods();

    /** 分页查询：按股票代码模糊 + 报告期筛选 */
    @Query("SELECT d FROM BankStockData d WHERE " +
            "(:stockCode IS NULL OR :stockCode = '' OR d.stockCode LIKE CONCAT('%', :stockCode, '%') OR d.stockName LIKE CONCAT('%', :stockCode, '%')) " +
            "AND (:reportPeriod IS NULL OR :reportPeriod = '' OR d.reportPeriod = :reportPeriod) " +
            "ORDER BY d.reportPeriod DESC, d.stockCode ASC")
    Page<BankStockData> searchByCondition(
            @Param("stockCode") String stockCode,
            @Param("reportPeriod") String reportPeriod,
            Pageable pageable);

    /** 分页查询：每只股票只返回最新一期数据 */
    @Query("SELECT d FROM BankStockData d WHERE d.reportPeriod = " +
            "(SELECT MAX(d2.reportPeriod) FROM BankStockData d2 WHERE d2.stockCode = d.stockCode) " +
            "AND (:stockCode IS NULL OR :stockCode = '' OR d.stockCode LIKE CONCAT('%', :stockCode, '%') OR d.stockName LIKE CONCAT('%', :stockCode, '%')) " +
            "ORDER BY d.stockCode ASC")
    Page<BankStockData> searchLatestByCondition(
            @Param("stockCode") String stockCode,
            Pageable pageable);

    /** 查询最新一期报告期 */
    @Query("SELECT d.reportPeriod FROM BankStockData d ORDER BY d.reportPeriod DESC LIMIT 1")
    String findLatestReportPeriod();
}
