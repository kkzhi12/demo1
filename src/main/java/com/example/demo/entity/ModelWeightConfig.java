package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型权重配置实体（持久化到数据库）
 *
 * @author : wangyuzhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "model_weight_config")
public class ModelWeightConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 策略名称 */
    @Column(name = "strategy_name", length = 50, nullable = false)
    private String strategyName;

    /** 盈利能力权重 */
    @Column(name = "profitability_weight", precision = 4, scale = 2)
    private BigDecimal profitabilityWeight;

    /** 资产质量权重 */
    @Column(name = "asset_quality_weight", precision = 4, scale = 2)
    private BigDecimal assetQualityWeight;

    /** 成长性权重 */
    @Column(name = "growth_weight", precision = 4, scale = 2)
    private BigDecimal growthWeight;

    /** 估值权重 */
    @Column(name = "valuation_weight", precision = 4, scale = 2)
    private BigDecimal valuationWeight;

    /** 资本充足权重 */
    @Column(name = "capital_weight", precision = 4, scale = 2)
    private BigDecimal capitalWeight;

    /** 是否默认策略 */
    @Column(name = "is_default")
    private Boolean isDefault;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
