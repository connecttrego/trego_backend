package com.trego.api;

import com.trego.dao.entity.Stock;
import com.trego.service.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockController {

    @Autowired
    IStockService stockService;

    @GetMapping("/stocks")
    public List<Stock> retrieveMedicines() {

        return stockService.findAll();
    }


}
