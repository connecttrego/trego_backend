package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trego.dao.entity.Vendor;
import com.trego.dto.MedicineDTO;
import com.trego.dto.MedicinePreOrderDTO;
import com.trego.dto.VendorDTO;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponseDTO {
    private Long vendorId;
    private double totalCartValue;
    private double amountToPay;
    private double discount;
    private  long orderId;
    private String name;
    private String licence;
    private String gstNumber;
   // private String address;
    private String logo;
    private String lat;
    private String lng;

    private List<MedicineDTO> medicine;
}
