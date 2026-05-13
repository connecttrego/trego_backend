package com.trego.service.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.MedicineInformation;
import com.trego.dto.MedicineDTO;
import com.trego.dao.entity.Stock;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.SubstituteDTO;
import com.trego.service.IMedicineService;
import com.trego.utils.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicineServiceImpl implements IMedicineService {

    @Autowired
    MedicineRepository medicineRepository;

    @Autowired
    StockRepository stockRepository;

    @Override
    public List<MedicineWithStockAndVendorDTO> findAll() {
        List<MedicineWithStockAndVendorDTO> medicineWithStockAndVendorDTOList = new ArrayList<>();
        List<Medicine> medicines = medicineRepository.findAll();
        for (Medicine medicine : medicines) {

            MedicineWithStockAndVendorDTO medicineWithStockAndVendorDTO = populateMedicineWithStockVendor(medicine);
            List<Stock> stocks = stockRepository.findByMedicineId(medicine.getId());
            medicineWithStockAndVendorDTO.setStocks(stocks);
            medicineWithStockAndVendorDTOList.add(medicineWithStockAndVendorDTO);
        }

        return medicineWithStockAndVendorDTOList;
    }

    @Override
    public MedicineDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id).orElse(null);
        if (medicine == null) return null;

        MedicineDTO medicineDTO = new MedicineDTO();
        medicineDTO.setId(medicine.getId());

        medicineDTO.setName(medicine.getName());
        medicineDTO.setManufacturer(medicine.getManufacture()); // Fixed rename
        medicineDTO.setSaltComposition(medicine.getSaltComposition());
        medicineDTO.setMedicineType(medicine.getMedicineType());

        MedicineInformation medicineInformation = medicine.getMedicineInformation();
        if (medicineInformation != null) {
            medicineDTO.setIntroduction(medicineInformation.getIntroduction());
            medicineDTO.setDescription(medicineInformation.getDescription());
            medicineDTO.setHowItWorks(medicineInformation.getHowItWorks());
            medicineDTO.setSafetyAdvise(medicineInformation.getSafetyAdvise());
            medicineDTO.setIfMiss(medicineInformation.getIfMiss());
            medicineDTO.setUseOf(medicineInformation.getUseOf());
            medicineDTO.setStrip(medicineInformation.getPacking());
            medicineDTO.setCommonSideEffect(medicineInformation.getCommonSideEffect());
            medicineDTO.setAlcoholInteraction(medicineInformation.getAlcoholInteraction());
            medicineDTO.setPregnancyInteraction(medicineInformation.getPregnancyInteraction());
            medicineDTO.setLactationInteraction(medicineInformation.getLactationInteraction());
            medicineDTO.setDrivingInteraction(medicineInformation.getDrivingInteraction());
            medicineDTO.setKidneyInteraction(medicineInformation.getKidneyInteraction());
            medicineDTO.setLiverInteraction(medicineInformation.getLiverInteraction());
            medicineDTO.setQuestionAnswers(medicineInformation.getQuestionAnswers());
            medicineDTO.setPhoto1(medicineInformation.getPhoto1());
        }

        medicineDTO.setPrescriptionRequired(medicine.getPrescriptionRequired());
        medicineDTO.setCountryOfOrigin(medicine.getCountryOfOrigin());
        
        List<Stock> stocks = stockRepository.findByMedicineId(medicine.getId());
        medicineDTO.setOffLineStocks(stocks);
        medicineDTO.setOnLineStocks(new ArrayList<>());

        return medicineDTO;
    }

    @Override
    public Page<MedicineWithStockAndVendorDTO> searchMedicines(String searchText, long vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicines = null;
        if (vendorId != 0) {
            medicines = medicineRepository.findByNameWithVendorId(searchText, (int) vendorId, pageable);

        } else {
            medicines = medicineRepository.findByNameContainingIgnoreCaseOrNameIgnoreCase(searchText, "", pageable);
        }
        return convertResponse(medicines);

    }


    @Override
    public Page<MedicineWithStockAndVendorDTO> getMedicinesBySubcategory(Integer subcategoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Medicine> medicines = medicineRepository.findBySubcategoryId(subcategoryId, pageable);

        return convertResponse(medicines);
    }


    private Page<MedicineWithStockAndVendorDTO> convertResponse(Page<Medicine> medicines) {
        Page<MedicineWithStockAndVendorDTO> medicineDTOs = medicines.map(medicine -> {
            MedicineWithStockAndVendorDTO medicineWithStockAndVendorDTO = populateMedicineWithStockVendor(medicine);
            medicineWithStockAndVendorDTO.setStocks(medicine.getStocks());
            return medicineWithStockAndVendorDTO;
        });
        return medicineDTOs;
    }

    private MedicineWithStockAndVendorDTO populateMedicineWithStockVendor(Medicine medicine) {
        MedicineWithStockAndVendorDTO medicineWithStockAndVendorDTO = new MedicineWithStockAndVendorDTO();
        medicineWithStockAndVendorDTO.setId(medicine.getId());
        medicineWithStockAndVendorDTO.setName(medicine.getName());
        medicineWithStockAndVendorDTO.setMedicineType(medicine.getMedicineType());
        medicineWithStockAndVendorDTO.setManufacturer(medicine.getManufacture()); // Fixed rename
        medicineWithStockAndVendorDTO.setSaltComposition(medicine.getSaltComposition());
        
        MedicineInformation medicineInformation = medicine.getMedicineInformation();
        if (medicineInformation != null) {
            medicineWithStockAndVendorDTO.setPhoto1(medicineInformation.getPhoto1());
            medicineWithStockAndVendorDTO.setUseOf(medicineInformation.getUseOf());
            medicineWithStockAndVendorDTO.setPacking(medicineInformation.getPacking());
        }
        
        SubstituteDTO substituteDTO = new SubstituteDTO();
        substituteDTO.setText("Substitute Available ");
        medicineWithStockAndVendorDTO.setSubstituteDTO(substituteDTO);
        return medicineWithStockAndVendorDTO;
    }
}