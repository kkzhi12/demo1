package com.example.demo.repository;

import com.example.demo.entity.StockFinanceData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockFinanceDataRepository extends JpaRepository<StockFinanceData, Long> {

    List<StockFinanceData> findByReportPeriod(String reportPeriod);

    List<StockFinanceData> findByStockCodeOrderByReportPeriodDesc(String stockCode);

    Optional<StockFinanceData> findByStockCodeAndReportPeriod(String stockCode, String reportPeriod);

    @Query("SELECT DISTINCT d.reportPeriod FROM StockFinanceData d ORDER BY d.reportPeriod DESC")
    List<String> findAllReportPeriods();

    /** 分页查询：每只股票只返回最新一期数据 */
    @Query("SELECT d FROM StockFinanceData d WHERE d.reportPeriod = " +
            "(SELECT MAX(d2.reportPeriod) FROM StockFinanceData d2 WHERE d2.stockCode = d.stockCode) " +
            "AND (:stockCode IS NULL OR :stockCode = '' OR d.stockCode LIKE CONCAT('%', :stockCode, '%') OR d.stockName LIKE CONCAT('%', :stockCode, '%')) " +
            "AND (:minRevenueYoy IS NULL OR d.revenueYoy >= :minRevenueYoy) " +
            "AND (:minNetProfitYoy IS NULL OR d.netProfitYoy >= :minNetProfitYoy) " +
            "AND (:minRoe IS NULL OR d.roe >= :minRoe) " +
            "ORDER BY d.stockCode ASC")
    Page<StockFinanceData> searchLatestByCondition(
            @Param("stockCode") String stockCode,
            @Param("minRevenueYoy") BigDecimal minRevenueYoy,
            @Param("minNetProfitYoy") BigDecimal minNetProfitYoy,
            @Param("minRoe") BigDecimal minRoe,
            Pageable pageable);
}
