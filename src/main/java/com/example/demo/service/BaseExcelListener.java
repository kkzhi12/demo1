package com.example.demo.service;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : wangyuzhi
 * @date: 2025/5/14  18:18
 */
@Slf4j
public class BaseExcelListener<T> extends AnalysisEventListener<T> {

    // 定义一个数据列表，用于存储读取到的每一行数据
    @Getter
    private final List<T> dataList = new ArrayList<>();

    //定义一个计数器
    private int count = 0;
    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        // 添加读取到的每一行数据到数据列表
        dataList.add(t);

        //计数器自增
        count++;
        if (count % 10000 == 0) {
            log.debug("已读取 {} 条数据", count);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 读取完成后，打印数据列表的大小
        log.debug("读取完成，共读取了 {} 条数据", dataList.size());
    }

}
