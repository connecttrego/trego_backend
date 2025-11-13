package com.trego.service;

import com.trego.dto.OtcProductDTO;
import java.util.List;

public interface IOtcProductService {
    List<OtcProductDTO> getProductsBySubcategoryId(Long subcategoryId);
}