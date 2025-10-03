package com.trego.service.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.SubstituteDTO;
import com.trego.dto.SubstituteDetailDTO;
import com.trego.dto.view.SubstituteDetailView;
import com.trego.service.IMedicineService;
import com.trego.service.ISubstituteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubstituteServiceImpl implements ISubstituteService {

    @Autowired
    MedicineRepository medicineRepository;

    @Autowired
    StockRepository stockRepository;

    @Override
    public List<SubstituteDetailView> findSubstitute(long id) {
        List<SubstituteDetailDTO> medicineWithStockAndVendorDTOList = new ArrayList<>();
        List<SubstituteDetailView> medicines = medicineRepository.findSubstituteByMedicineId(id);
//        for (Medicine medicine : medicines) {
//
//            SubstituteDetailDTO medicineWithStockAndVendorDTO = populateMedicineWithStockVendor(medicine);
//            //List<Stock> stocks = stockRepository.findByMedicineId(medicine.getId());
//            //medicineWithStockAndVendorDTO.setStocks(stocks);
//            medicineWithStockAndVendorDTOList.add(medicineWithStockAndVendorDTO);
//        }

        return medicines;
    }

//    private SubstituteDetailDTO populateMedicineWithStockVendor(Medicine medicine) {
//        SubstituteDetailDTO dto = new SubstituteDetailDTO();
//
//        // basic fields from medicine entity
//        dto.setId(medicine.getId());
//        dto.setName(medicine.getName());
//        dto.setManufacturers(medicine.getManufacturer());
//        dto.setSaltComposition(medicine.getSaltComposition());
//        dto.setMedicineType(medicine.getMedicineType());
//        //dto.setIntroduction(medicine.getIntroduction());
//        //dto.setDescription(medicine.getDescription());
//        //dto.setHowToUse(medicine.getHowItWorks()); // if you store "howItWorks" in DB
//        //dto.setSafetyAdvise(medicine.getSafetyAdvise());
//        //dto.setIfMiss(medicine.getIfMiss());
//        dto.setPhoto1(medicine.getPhoto1());
//        dto.setPackaging(medicine.getPacking());
//        dto.setPackagingType(medicine.getPackagingType());
//        dto.setPrescriptionRequired(medicine.getPrescriptionRequired());
//        dto.setStorage(medicine.getStorage());
//        dto.setUseOf(medicine.getUseOf());
//        dto.setCommonSideEffect(medicine.getCommonSideEffect());
//        dto.setAlcoholInteraction(medicine.getAlcoholInteraction());
//        dto.setPregnancyInteraction(medicine.getPregnancyInteraction());
//        dto.setLactationInteraction(medicine.getLactationInteraction());
//        dto.setDrivingInteraction(medicine.getDrivingInteraction());
//        dto.setKidneyInteraction(medicine.getKidneyInteraction());
//        dto.setLiverInteraction(medicine.getLiverInteraction());
//        dto.setManufacturerAddress(medicine.getManufacturerAddress());
//        dto.setCountryOfOrigin(medicine.getCountryOfOrigin());
//        //dto.setQa(medicine.getQuestionAnswers());
//        // you can also map photo1…photo4 if you add fields in DTO
//
//        // now handle stock/vendor-related data (example: take the first stock)
////        if (medicine.getStocks() != null && !medicine.getStocks().isEmpty()) {
////            Stock stock = medicine.getStocks().get(0);  // pick first stock or loop all
////            // assuming Stock entity has:
////            // BigDecimal mrp, BigDecimal bestPrice, BigDecimal discountPercent, Integer views, Integer bought, Vendor vendor
////            dto.setMrp(stock.getMrp());
////            dto.setBestPrice(stock.getBestPrice());
////            dto.setDiscountPercent(stock.getDiscountPercent());
////            dto.setViews(stock.getViews());
////            dto.setBought(stock.getBought());
////            dto.setStock(stock.getQuantity()); // if you have quantity
////            // if you want vendor name/label:
////            if (stock.getVendor() != null) {
////                dto.setLabel(stock.getVendor().getName()); // or vendor label field
////            }
////        }
//        return dto;
//    }

}