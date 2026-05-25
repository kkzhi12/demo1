package com.example.demo.rest;

import com.example.demo.entity.BankStockScore;
import com.example.demo.entity.ModelWeight;
import com.example.demo.service.BankStockAnalysisService;
import com.example.demo.service.BankStockSampleData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 银行股分析演示接口
 * 使用内置示例数据，无需上传Excel即可体验模型效果
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/bank-stock/demo")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BankStockDemoController {

    private final BankStockAnalysisService bankStockAnalysisService;

    /**
     * 使用示例数据运行分析（默认权重）
     * GET /api/v1/bank-stock/demo/run
     */
    @GetMapping("/run")
    public List<BankStockScore> runDemo() {
        return bankStockAnalysisService.analyze(
                BankStockSampleData.getSampleData(),
                ModelWeight.defaultWeight()
        );
    }

    /**
     * 使用示例数据运行分析（自定义权重）
     * POST /api/v1/bank-stock/demo/run-custom
     *
     * 示例：偏重成长性的配置
     * {
     *   "profitabilityWeight": 0.20,
     *   "assetQualityWeight": 0.25,
     *   "growthWeight": 0.35,
     *   "valuationWeight": 0.10,
     *   "capitalWeight": 0.10
     * }
     */
    @PostMapping("/run-custom")
    public List<BankStockScore> runDemoCustom(@RequestBody ModelWeight weight) {
        return bankStockAnalysisService.analyze(
                BankStockSampleData.getSampleData(),
                weight
        );
    }

    /**
     * 偏重价值/高股息策略
     * 适合追求稳定分红收益的投资者
     */
    @GetMapping("/strategy/dividend")
    public List<BankStockScore> dividendStrategy() {
        ModelWeight weight = ModelWeight.builder()
                .profitabilityWeight(0.20)
                .assetQualityWeight(0.25)
                .growthWeight(0.10)
                .valuationWeight(0.35)  // 加大估值权重
                .capitalWeight(0.10)
                .build();
        return bankStockAnalysisService.analyze(
                BankStockSampleData.getSampleData(), weight);
    }

    /**
     * 偏重成长性策略
     * 适合追求资本增值的投资者
     */
    @GetMapping("/strategy/growth")
    public List<BankStockScore> growthStrategy() {
        ModelWeight weight = ModelWeight.builder()
                .profitabilityWeight(0.25)
                .assetQualityWeight(0.20)
                .growthWeight(0.35)  // 加大成长权重
                .valuationWeight(0.10)
                .capitalWeight(0.10)
                .build();
        return bankStockAnalysisService.analyze(
                BankStockSampleData.getSampleData(), weight);
    }

    /**
     * 偏重安全性策略
     * 适合风险厌恶型投资者
     */
    @GetMapping("/strategy/safety")
    public List<BankStockScore> safetyStrategy() {
        ModelWeight weight = ModelWeight.builder()
                .profitabilityWeight(0.20)
                .assetQualityWeight(0.35)  // 加大资产质量权重
                .growthWeight(0.15)
                .valuationWeight(0.15)
                .capitalWeight(0.15)  // 加大资本充足权重
                .build();
        return bankStockAnalysisService.analyze(
                BankStockSampleData.getSampleData(), weight);
    }

}
