package com.example.demo.rest;

import com.example.demo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author : wangyuzhi
 * @date: 2025/6/5  15:12
 */

@RestController
@RequestMapping("/api/v1/ccp/stock")
@RequiredArgsConstructor
public class StockRes {

    private final StockService stockService;

    @PostMapping("/importExcel")
    public void importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        stockService.importExcel(file);
    }

}
