package com.example.demo.repository;

import com.example.demo.entity.ModelWeightConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 模型权重配置 Repository
 *
 * @author : wangyuzhi
 */
@Repository
public interface ModelWeightConfigRepository extends JpaRepository<ModelWeightConfig, Long> {

    /** 查询默认策略 */
    Optional<ModelWeightConfig> findByIsDefaultTrue();

    /** 根据策略名称查询 */
    Optional<ModelWeightConfig> findByStrategyName(String strategyName);
}
