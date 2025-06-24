package com.trego.service.impl;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import com.trego.dto.MedicineDTO;
import com.trego.dto.StockDTO;
import com.trego.dto.VendorDTO;
import com.trego.service.IVendorService;
import com.trego.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VendorServiceImpl implements IVendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    public List<VendorDTO> findVendorsByType(String type) {
        List<VendorDTO> vendorDTOs = new ArrayList<>();

        List<Vendor>  vendors = vendorRepository.findByCategory(type); // This will fetch all vendors
        for (Vendor vendor : vendors){
            VendorDTO vendorDTO = new VendorDTO();
            vendorDTO.setId(vendor.getId());
            vendorDTO.setName(vendor.getName());
            if(vendor.getCategory().equalsIgnoreCase("retail")) {
                vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.OFFLINE_BASE_URL+ vendor.getLogo());
            }else{
                vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.ONLINE_BASE_URL+ vendor.getLogo());
            }
            vendorDTOs.add(vendorDTO);
        }
        return  vendorDTOs;
    }

    @Override
    public VendorDTO getVendorByIdOrMedicine(Long id,  String searchText, int page, int size) {
        VendorDTO vendorDTO = new VendorDTO();
        Vendor vendor = vendorRepository.findById(id).orElse(null);
        vendorDTO.setId(vendor.getId());
        vendorDTO.setName(vendor.getName());
        if(vendor.getCategory().equalsIgnoreCase("retail")) {
            vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.OFFLINE_BASE_URL+ vendor.getLogo());
        }else{
            vendorDTO.setLogo(Constants.LOGO_BASE_URL + Constants.ONLINE_BASE_URL+ vendor.getLogo());
        }
        vendorDTO.setGstNumber(vendor.getGistin());
        vendorDTO.setLicence(vendor.getDruglicense());
        vendorDTO.setAddress(vendor.getAddress());
        vendorDTO.setLat(vendor.getLat());
        vendorDTO.setLng(vendor.getLng());

        List<StockDTO> stockDTOS = new ArrayList<>();

        // Create a Pageable object
        Pageable pageable = PageRequest.of(page, size);
        // Fetch paginated stocks
        Page<Stock> stocksPage = stockRepository.findByVendorId(vendor.getId(), pageable);
        // Get the list of stocks from the page
        List<Stock> stocks = stocksPage.getContent();


        //List<Stock> stocks   = stockRepository.findByVendorId(vendor.getId());
        List<MedicineDTO> medicineDTOList = new ArrayList<>();
        for(Stock stock : stocks){
            Medicine medicine = stock.getMedicine();
            Pattern pattern = Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(medicine.getName());
            if (matcher.find()) {
                MedicineDTO medicineDTO = new MedicineDTO();
                medicineDTO.setId(medicine.getId());
                medicineDTO.setName(medicine.getName());
                medicineDTO.setManufacturer(medicine.getManufacturer());
                medicineDTO.setMedicineType(medicine.getMedicineType());
                medicineDTO.setDescription(medicine.getDescription());
                medicineDTO.setSaltComposition(medicine.getSaltComposition());
                medicineDTO.setPhoto1(Constants.LOGO_BASE_URL  + Constants.MEDICINES_BASE_URL + medicine.getPhoto1());
                medicineDTO.setDiscount(stock.getDiscount());
                medicineDTO.setQty(stock.getQty());
                medicineDTO.setMrp(stock.getMrp());
                medicineDTO.setExpiryDate(stock.getExpiryDate());
                medicineDTOList.add(medicineDTO);
            }
        }
        vendorDTO.setMedicines(medicineDTOList);
        return vendorDTO;

    }
}
