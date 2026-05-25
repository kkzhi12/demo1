package com.example.demo.repository;

import com.example.demo.entity.StockFinanceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockFinanceDataRepository extends JpaRepository<StockFinanceData, Long> {

    List<StockFinanceData> findByReportPeriod(String reportPeriod);

    List<StockFinanceData> findByStockCodeOrderByReportPeriodDesc(String stockCode);

    Optional<StockFinanceData> findByStockCodeAndReportPeriod(String stockCode, String reportPeriod);

    @Query("SELECT DISTINCT d.reportPeriod FROM StockFinanceData d ORDER BY d.reportPeriod DESC")
    List<String> findAllReportPeriods();
}
