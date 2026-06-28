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

    @Autowired
    com.trego.dao.impl.MasterMedicineRepository masterMedicineRepository;

    @Autowired
    com.trego.dao.impl.VendorRepository vendorRepository;

    @Override
    public List<MedicineWithStockAndVendorDTO> findAll() {
        List<MedicineWithStockAndVendorDTO> medicineWithStockAndVendorDTOList = new ArrayList<>();
        List<Medicine> medicines = medicineRepository.findAll();
        for (Medicine medicine : medicines) {
            MedicineWithStockAndVendorDTO medicineWithStockAndVendorDTO = populateMedicineWithStockVendor(medicine);
            medicineWithStockAndVendorDTOList.add(medicineWithStockAndVendorDTO);
        }
        return medicineWithStockAndVendorDTOList;
    }

    @Override
    public MedicineDTO getMedicineById(Long id) {
        // Try looking up in master catalog first (since frontend searches return master medicine IDs)
        com.trego.dao.entity.MasterMedicine masterMedicine = masterMedicineRepository.findById(id.intValue()).orElse(null);
        if (masterMedicine != null) {
            MedicineDTO medicineDTO = new MedicineDTO();
            medicineDTO.setId(masterMedicine.getMedicineId().longValue());
            medicineDTO.setName(masterMedicine.getName());
            medicineDTO.setManufacturer(masterMedicine.getManufacture());
            medicineDTO.setSaltComposition(masterMedicine.getSaltComposition());
            medicineDTO.setMedicineType(masterMedicine.getMedicineType());
            medicineDTO.setIntroduction(masterMedicine.getIntroduction());
            medicineDTO.setDescription(masterMedicine.getDescription());
            medicineDTO.setHowItWorks(masterMedicine.getHowItWorks() != null ? masterMedicine.getHowItWorks() : masterMedicine.getHowWorks());
            medicineDTO.setSafetyAdvise(masterMedicine.getSafetyAdvice() != null ? masterMedicine.getSafetyAdvice() : masterMedicine.getSafetyAdvise());
            medicineDTO.setIfMiss(masterMedicine.getIfMiss());
            medicineDTO.setUseOf(masterMedicine.getUseOf());
            medicineDTO.setStrip(masterMedicine.getPackaging());
            medicineDTO.setCommonSideEffect(masterMedicine.getCommonSideEffect());
            medicineDTO.setAlcoholInteraction(masterMedicine.getAlcoholInteraction());
            medicineDTO.setPregnancyInteraction(masterMedicine.getPregnancyInteraction());
            medicineDTO.setLactationInteraction(masterMedicine.getLactationInteraction());
            medicineDTO.setDrivingInteraction(masterMedicine.getDrivingInteraction());
            medicineDTO.setKidneyInteraction(masterMedicine.getKidneyInteraction());
            medicineDTO.setLiverInteraction(masterMedicine.getLiverInteraction());
            medicineDTO.setQuestionAnswers(masterMedicine.getQuestionAnswers());
            
            String photo1 = Constants.getMedicineImageWithFallback(masterMedicine.getPhoto1());
            medicineDTO.setPhoto1(photo1);
            medicineDTO.setImage(photo1);
            
            medicineDTO.setPrescriptionRequired(masterMedicine.getPrescriptionRequired());
            medicineDTO.setCountryOfOrigin(masterMedicine.getCountryOfOrigin());
            
            // Find all vendor medicines associated with this master medicine
            List<Medicine> vendorMedicines = medicineRepository.findByMedicineId(masterMedicine.getMedicineId());
            List<Stock> allStocks = new ArrayList<>();
            for (Medicine vm : vendorMedicines) {
                List<Stock> stocks = stockRepository.findByMedicineId(vm.getId());
                if (stocks != null) {
                    for (Stock s : stocks) {
                        if (s.getVendor() == null && s.getRawVendorId() != null) {
                            com.trego.dao.entity.Vendor v = vendorRepository.findById(s.getRawVendorId()).orElse(null);
                            if (v == null) {
                                v = vendorRepository.findByVendorId(s.getRawVendorId()).orElse(null);
                            }
                            s.setVendor(v);
                        }
                    }
                    allStocks.addAll(stocks);
                }
            }
            medicineDTO.setOffLineStocks(allStocks);
            medicineDTO.setOnLineStocks(new ArrayList<>());
            
            return medicineDTO;
        }

        // Fallback to legacy vendor medicine lookup if not found in master catalog
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
            medicineDTO.setPhoto1(Constants.getMedicineImageWithFallback(medicineInformation.getPhoto1()));
            medicineDTO.setImage(Constants.getMedicineImageWithFallback(medicineInformation.getPhoto1()));
        }

        medicineDTO.setPrescriptionRequired(medicine.getPrescriptionRequired());
        medicineDTO.setCountryOfOrigin(medicine.getCountryOfOrigin());

        List<Stock> stocks = stockRepository.findByMedicineId(medicine.getId());
        if (stocks != null) {
            for (Stock s : stocks) {
                if (s.getVendor() == null && s.getRawVendorId() != null) {
                    com.trego.dao.entity.Vendor v = vendorRepository.findById(s.getRawVendorId()).orElse(null);
                    if (v == null) {
                        v = vendorRepository.findByVendorId(s.getRawVendorId()).orElse(null);
                    }
                    s.setVendor(v);
                }
            }
        }
        medicineDTO.setOffLineStocks(stocks);
        medicineDTO.setOnLineStocks(new ArrayList<>());

        return medicineDTO;
    }

    @Override
    public Page<MedicineWithStockAndVendorDTO> searchMedicines(String searchText, Integer vendorId, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.trego.dao.entity.MasterMedicine> masterMedicines = masterMedicineRepository.findByNameContainingIgnoreCase(searchText, pageable);
        return masterMedicines.map(this::convertMasterToDto);
    }

    private MedicineWithStockAndVendorDTO convertMasterToDto(com.trego.dao.entity.MasterMedicine masterMedicine) {
        MedicineWithStockAndVendorDTO dto = new MedicineWithStockAndVendorDTO();
        dto.setId(Long.valueOf(masterMedicine.getMedicineId()));
        dto.setName(masterMedicine.getName());
        dto.setManufacturer(masterMedicine.getManufacture());
        dto.setSaltComposition(masterMedicine.getSaltComposition());
        dto.setPhoto1(Constants.getMedicineImageWithFallback(masterMedicine.getPhoto1()));
        dto.setPacking(masterMedicine.getPackaging());
        dto.setUseOf(masterMedicine.getUseOf());
        dto.setMedicineType(masterMedicine.getMedicineType());

        // Find all vendor medicines and stocks
        List<Medicine> vendorMedicines = medicineRepository.findByMedicineId(masterMedicine.getMedicineId());
        List<Stock> allStocks = new ArrayList<>();
        for (Medicine vm : vendorMedicines) {
            List<Stock> stocks = stockRepository.findByMedicineId(vm.getId());
            if (stocks != null) {
                for (Stock s : stocks) {
                    if (s.getVendor() == null && s.getRawVendorId() != null) {
                        com.trego.dao.entity.Vendor v = vendorRepository.findById(s.getRawVendorId()).orElse(null);
                        if (v == null) {
                            v = vendorRepository.findByVendorId(s.getRawVendorId()).orElse(null);
                        }
                        s.setVendor(v);
                    }
                }
                allStocks.addAll(stocks);
            }
        }
        
        // Sort stocks by sellingPrice ASC so the cheapest is first (useful for card price display!)
        allStocks.sort((s1, s2) -> {
            Double p1 = s1.getDiscount() != null && s1.getMrp() != null ? (s1.getMrp() - s1.getDiscount()) : (s1.getMrp() != null ? s1.getMrp() : 0.0);
            Double p2 = s2.getDiscount() != null && s2.getMrp() != null ? (s2.getMrp() - s2.getDiscount()) : (s2.getMrp() != null ? s2.getMrp() : 0.0);
            return p1.compareTo(p2);
        });

        dto.setStocks(allStocks);
        dto.setSubstitutes(new ArrayList<>());
        
        SubstituteDTO subDto = new SubstituteDTO();
        subDto.setText("Substitute Available");
        dto.setSubstituteDTO(subDto);

        return dto;
    }


    @Override
    public Page<MedicineWithStockAndVendorDTO> getMedicinesBySubcategory(Long subcategoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Medicine> medicines = medicineRepository.findBySubcategoryId(subcategoryId.intValue(), pageable);

        return convertResponse(medicines);
    }

    private Page<MedicineWithStockAndVendorDTO> convertResponse(Page<Medicine> medicines) {
        Page<MedicineWithStockAndVendorDTO> medicineDTOs = medicines.map(medicine -> {
            MedicineWithStockAndVendorDTO medicineWithStockAndVendorDTO = populateMedicineWithStockVendor(medicine);
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
    public List<VendorMedicinePriceResponseDTO> searchMedicineVendorPrices(Long medicineId) {
        // Fetch Master Medicine details to fallback on for missing catalog information
        com.trego.dao.entity.MasterMedicine masterMed = masterMedicineRepository.findById(medicineId.intValue()).orElse(null);

        // Direct FK lookup: vendor_medicine.medicine_id → medicine_master_db_table.medicine_id
        List<VendorMedicinePriceView> views = medicineRepository.searchMedicineVendorPrices(Math.toIntExact(medicineId));

        // Use LinkedHashMap to preserve insertion order (already sorted by selling
        // price ASC from DB)
        Map<Long, VendorMedicinePriceResponseDTO> medicineMap = new LinkedHashMap<>();

        for (VendorMedicinePriceView view : views) {
            Long viewMedicineId = view.getMedicineId();

            // Get or create the medicine-level response
            VendorMedicinePriceResponseDTO medicineResponse = medicineMap.computeIfAbsent(viewMedicineId, id -> {
                VendorMedicinePriceResponseDTO dto = new VendorMedicinePriceResponseDTO();
                dto.setMedicineId(view.getMedicineId());
                dto.setMedicineName(masterMed != null ? masterMed.getName() : view.getMedicineName());
                dto.setManufacturer(view.getManufacturer() != null && !view.getManufacturer().trim().isEmpty() ? view.getManufacturer() : (masterMed != null ? masterMed.getManufacture() : ""));
                dto.setSaltComposition(view.getSaltComposition() != null && !view.getSaltComposition().trim().isEmpty() ? view.getSaltComposition() : (masterMed != null ? masterMed.getSaltComposition() : ""));
                dto.setPhoto1(Constants.getMedicineImageWithFallback(view.getPhoto1() != null && !view.getPhoto1().trim().isEmpty() ? view.getPhoto1() : (masterMed != null ? masterMed.getPhoto1() : "")));
                dto.setPacking(view.getPacking() != null && !view.getPacking().trim().isEmpty() ? view.getPacking() : (masterMed != null && masterMed.getPackaging() != null ? masterMed.getPackaging() : ""));
                dto.setUseOf(view.getUseOf() != null && !view.getUseOf().trim().isEmpty() ? view.getUseOf() : (masterMed != null && masterMed.getUseOf() != null ? masterMed.getUseOf() : ""));
                dto.setVendorPrices(new ArrayList<>());
                return dto;
            });

            // Build vendor price entry
            VendorPriceDTO vendorPrice = new VendorPriceDTO();
            vendorPrice.setStockId(view.getStockId());
            vendorPrice.setVendorId(view.getVendorId());
            vendorPrice.setVendorName(view.getVendorName());
            vendorPrice.setVendorLogo(Constants.getVendorLogoWithFallback(view.getVendorLogo()));
            vendorPrice.setVendorRating(view.getVendorRating() != null ? view.getVendorRating() : "0");
            vendorPrice.setDeliveryTimeMinutes(view.getDeliveryTime() != null ? view.getDeliveryTime() : 0);
            vendorPrice.setMrp(view.getMrp());
            vendorPrice.setDiscount(view.getDiscount());
            vendorPrice.setSellingPrice(view.getSellingPrice());
            vendorPrice.setQty(view.getQty());
            vendorPrice.setExpiryDate(view.getExpiryDate() != null ? view.getExpiryDate() : "");
            vendorPrice.setPacking(view.getPacking() != null ? view.getPacking() : "");

            medicineResponse.getVendorPrices().add(vendorPrice);
        }

        // Set total vendor count for each medicine
        medicineMap.values().forEach(dto -> {
            dto.setTotalVendors(dto.getVendorPrices().size());
        });

        return new ArrayList<>(medicineMap.values());
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
            medicineWithStockAndVendorDTO.setPhoto1(Constants.getMedicineImageWithFallback(medicineInformation.getPhoto1()));
            medicineWithStockAndVendorDTO.setUseOf(medicineInformation.getUseOf());
            medicineWithStockAndVendorDTO.setPacking(medicineInformation.getPacking());
        }

        SubstituteDTO substituteDTO = new SubstituteDTO();
        substituteDTO.setText("Substitute Available ");
        medicineWithStockAndVendorDTO.setSubstituteDTO(substituteDTO);
        return medicineWithStockAndVendorDTO;
    }
}