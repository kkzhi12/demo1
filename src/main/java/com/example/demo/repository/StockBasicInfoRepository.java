package com.example.demo.repository;

import com.example.demo.entity.StockBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockBasicInfoRepository extends JpaRepository<StockBasicInfo, Long> {

    /** 按代码或名称模糊搜索 */
    List<StockBasicInfo> findByStockCodeContainingOrStockNameContaining(String code, String name);
}
