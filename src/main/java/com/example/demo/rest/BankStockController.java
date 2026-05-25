package com.example.demo.rest;

import com.example.demo.entity.BankStock;
import com.example.demo.entity.BankStockScore;
import com.example.demo.entity.ModelWeight;
import com.example.demo.service.BankStockAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 银行股价值分析接口
 *
 * @author : wangyuzhi
 */
@RestController
@RequestMapping("/api/v1/bank-stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BankStockController {

    private final BankStockAnalysisService bankStockAnalysisService;

    /**
     * 上传Excel文件进行银行股分析（使用默认权重）
     */
    @PostMapping("/analyze")
    public List<BankStockScore> analyze(@RequestParam("file") MultipartFile file) {
        return bankStockAnalysisService.analyzeFromExcel(file, ModelWeight.defaultWeight());
    }

    /**
     * 通过JSON数据进行银行股分析（前端手动输入）
     *
     * @param request 包含 bankStocks 和可选的 weight
     * @return 评分排名结果
     */
    @PostMapping("/analyze-json")
    public List<BankStockScore> analyzeFromJson(@RequestBody AnalyzeRequest request) {
        ModelWeight weight = request.getWeight() != null ? request.getWeight() : ModelWeight.defaultWeight();
        return bankStockAnalysisService.analyze(request.getBankStocks(), weight);
    }

    /**
     * 获取默认权重配置（方便前端展示和调整）
     */
    @GetMapping("/default-weight")
    public ModelWeight getDefaultWeight() {
        return ModelWeight.defaultWeight();
    }

    /**
     * 请求体封装
     */
    @lombok.Data
    public static class AnalyzeRequest {
        private List<BankStock> bankStocks;
        private ModelWeight weight;
    }

}
