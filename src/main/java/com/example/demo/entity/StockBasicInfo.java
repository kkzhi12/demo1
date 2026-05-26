package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 股票基本信息表
 * 存储所有A股的代码和名称，只需加载一次
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_basic_info")
public class StockBasicInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 股票代码 */
    @Column(name = "stock_code", length = 10, nullable = false, unique = true)
    private String stockCode;

    /** 股票名称 */
    @Column(name = "stock_name", length = 50, nullable = false)
    private String stockName;

    /** 更新时间 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
