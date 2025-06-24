package com.trego.api;

import com.trego.dto.VendorDTO;
import com.trego.service.IVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VendorController {

    @Autowired
    IVendorService vendorService;

    @GetMapping("/vendors")
    public List<VendorDTO> retrieveAllVendors(@RequestParam String type) {
        return vendorService.findVendorsByType(type);
    }


    @GetMapping("/vendors/{id}")
    public VendorDTO retrieveVendorById(@PathVariable Long id ,
                                        @RequestParam(required = false, defaultValue = "") String searchText ,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {


        return vendorService.getVendorByIdOrMedicine(id, searchText, page, size);
    }

}
