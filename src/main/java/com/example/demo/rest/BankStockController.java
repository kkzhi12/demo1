package com.example.demo.rest;

import com.example.demo.entity.BankStockScore;
import com.example.demo.entity.ModelWeight;
import com.example.demo.service.BankStockAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 银行股价值分析接口
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/bank-stock")
@RequiredArgsConstructor
public class BankStockController {

    private final BankStockAnalysisService bankStockAnalysisService;

    /**
     * 上传Excel文件进行银行股分析（使用默认权重）
     *
     * Excel格式要求：
     * 列名：股票代码, 股票名称, 当前股价, ROE, 净息差, 成本收入比,
     *       不良贷款率, 拨备覆盖率, 关注类贷款占比,
     *       营收增长率, 净利润增长率, 贷款余额增长率,
     *       PB, PE, 股息率, 核心一级资本充足率, 资本充足率
     */
    @PostMapping("/analyze")
    public List<BankStockScore> analyze(@RequestParam("file") MultipartFile file) {
        return bankStockAnalysisService.analyzeFromExcel(file, ModelWeight.defaultWeight());
    }

    /**
     * 上传Excel文件进行银行股分析（自定义权重）
     *
     * 可以通过请求体传入自定义权重配置
     */
    @PostMapping("/analyze-custom")
    public List<BankStockScore> analyzeWithCustomWeight(
            @RequestParam("file") MultipartFile file,
            @RequestBody ModelWeight weight) {
        return bankStockAnalysisService.analyzeFromExcel(file, weight);
    }

    /**
     * 获取默认权重配置（方便前端展示和调整）
     */
    @GetMapping("/default-weight")
    public ModelWeight getDefaultWeight() {
        return ModelWeight.defaultWeight();
    }

}
