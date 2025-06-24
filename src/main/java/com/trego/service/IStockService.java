package com.trego.service;

import com.trego.dao.entity.Stock;

import java.util.List;

public interface IStockService {
    List<Stock> findAll();
}