package com.example.demo.repository;

import com.example.demo.entity.BankStockAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 银行股分析结果 Repository
 *
 * @author : wangyuzhi
 */
@Repository
public interface BankStockAnalysisRepository extends JpaRepository<BankStockAnalysis, Long> {

    /** 根据批次号查询分析结果 */
    List<BankStockAnalysis> findByAnalysisBatchOrderByRankAsc(String analysisBatch);
}
