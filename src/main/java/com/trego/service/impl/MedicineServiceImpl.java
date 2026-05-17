package com.trego.service.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.MedicineInformation;
import com.trego.dto.MedicineDTO;
import com.trego.dao.entity.Stock;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dto.MedicineWithStockAndVendorDTO;
import com.trego.dto.SubstituteDTO;
import com.trego.dto.response.VendorMedicinePriceResponseDTO;
import com.trego.dto.response.VendorPriceDTO;
import com.trego.dto.view.VendorMedicinePriceView;
import com.trego.service.IMedicineService;
import com.trego.utils.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        if (medicine == null)
            return null;

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
    public Page<MedicineWithStockAndVendorDTO> searchMedicines(String searchText, Integer vendorId, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicines = null;
        if (vendorId != 0) {
            medicines = medicineRepository.findByNameWithVendorId(searchText, vendorId, pageable);

        } else {
            medicines = medicineRepository.findByNameContainingIgnoreCaseOrNameIgnoreCase(searchText, "", pageable);
        }
        return convertResponse(medicines);

    }

    @Override
    public Page<MedicineWithStockAndVendorDTO> getMedicinesBySubcategory(Long subcategoryId, int page, int size) {
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

    /**
     * Search medicines by name and return a list grouped by medicine.
     * Each medicine entry contains all vendors selling it, sorted by cheapest
     * selling price first.
     *
     * Logic flow:
     * 1. Query DB using native SQL to get all (medicine, vendor, stock, price) rows
     * matching the search text
     * 2. Results are already sorted by sellingPrice ASC from the query
     * 3. Group results by medicineId using LinkedHashMap to preserve insertion
     * (price-sorted) order
     * 4. Build VendorPriceDTO for each row and add to the medicine's vendorPrices
     * list
     * 5. Return the list of VendorMedicinePriceResponseDTO
     */
    @Override
    public List<VendorMedicinePriceResponseDTO> searchMedicineVendorPrices(String searchText) {
        List<VendorMedicinePriceView> views = medicineRepository.searchMedicineVendorPrices(searchText);

        // Use LinkedHashMap to preserve insertion order (already sorted by selling
        // price ASC from DB)
        Map<Long, VendorMedicinePriceResponseDTO> medicineMap = new LinkedHashMap<>();

        for (VendorMedicinePriceView view : views) {
            Long medicineId = view.getMedicineId();

            // Get or create the medicine-level response
            VendorMedicinePriceResponseDTO medicineResponse = medicineMap.computeIfAbsent(medicineId, id -> {
                VendorMedicinePriceResponseDTO dto = new VendorMedicinePriceResponseDTO();
                dto.setMedicineId(view.getMedicineId());
                dto.setMedicineName(view.getMedicineName());
                dto.setManufacturer(view.getManufacturer());
                dto.setSaltComposition(view.getSaltComposition());
                dto.setPhoto1(view.getPhoto1());
                dto.setPacking(view.getPacking());
                dto.setUseOf(view.getUseOf());
                dto.setVendorPrices(new ArrayList<>());
                return dto;
            });

            // Build vendor price entry
            VendorPriceDTO vendorPrice = new VendorPriceDTO();
            vendorPrice.setStockId(view.getStockId());
            vendorPrice.setVendorId(view.getVendorId());
            vendorPrice.setVendorName(view.getVendorName());
            vendorPrice.setVendorLogo(Constants.getVendorLogoWithFallback(view.getVendorLogo()));
            vendorPrice.setVendorRating(view.getVendorRating());
            vendorPrice.setDeliveryTimeMinutes(view.getDeliveryTime());
            vendorPrice.setMrp(view.getMrp());
            vendorPrice.setDiscount(view.getDiscount());
            vendorPrice.setSellingPrice(view.getSellingPrice());
            vendorPrice.setQty(view.getQty());
            vendorPrice.setExpiryDate(view.getExpiryDate());
            vendorPrice.setPacking(view.getPacking());

            medicineResponse.getVendorPrices().add(vendorPrice);
        }

        // Set total vendor count for each medicine
        medicineMap.values().forEach(dto -> {
            dto.setTotalVendors(dto.getVendorPrices().size());
        });

        return new ArrayList<>(medicineMap.values());
    }

    /**
     * Get a single medicine by ID with all vendors selling it.
     * Groups all vendor prices under one medicine response.
     *
     * Logic:
     * 1. Query DB using medicineId to get all (vendor, stock, price) rows
     * 2. Results are already sorted by sellingPrice ASC from the query
     * 3. Build single VendorMedicinePriceResponseDTO with medicine info
     * 4. Populate vendorPrices list with all vendors for this medicine
     *
     * @return VendorMedicinePriceResponseDTO with all vendor prices, or null if
     *         medicine not found
     */
    @Override
    public VendorMedicinePriceResponseDTO getMedicineVendorPrices(Long medicineId) {
        List<VendorMedicinePriceView> views = medicineRepository.getVendorsForMedicine(medicineId);

        if (views.isEmpty()) {
            return null;
        }

        // First row gives us the medicine info
        VendorMedicinePriceView firstView = views.get(0);
        VendorMedicinePriceResponseDTO response = new VendorMedicinePriceResponseDTO();
        response.setMedicineId(firstView.getMedicineId());
        response.setMedicineName(firstView.getMedicineName());
        response.setManufacturer(firstView.getManufacturer());
        response.setSaltComposition(firstView.getSaltComposition());
        response.setPhoto1(firstView.getPhoto1());
        response.setPacking(firstView.getPacking());
        response.setUseOf(firstView.getUseOf());
        response.setVendorPrices(new ArrayList<>());

        for (VendorMedicinePriceView view : views) {
            VendorPriceDTO vendorPrice = new VendorPriceDTO();
            vendorPrice.setStockId(view.getStockId());
            vendorPrice.setVendorId(view.getVendorId());
            vendorPrice.setVendorName(view.getVendorName());
            vendorPrice.setVendorLogo(Constants.getVendorLogoWithFallback(view.getVendorLogo()));
            vendorPrice.setVendorRating(view.getVendorRating());
            vendorPrice.setDeliveryTimeMinutes(view.getDeliveryTime());
            vendorPrice.setMrp(view.getMrp());
            vendorPrice.setDiscount(view.getDiscount());
            vendorPrice.setSellingPrice(view.getSellingPrice());
            vendorPrice.setQty(view.getQty());
            vendorPrice.setExpiryDate(view.getExpiryDate());
            vendorPrice.setPacking(view.getPacking());

            response.getVendorPrices().add(vendorPrice);
        }

        response.setTotalVendors(response.getVendorPrices().size());

        return response;
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