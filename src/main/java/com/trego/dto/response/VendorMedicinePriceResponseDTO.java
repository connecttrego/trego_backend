package com.trego.dto.response;

import lombok.Data;
import java.util.List;

/**
 * Response DTO for medicine vendor comparison search.
 * When a user searches for a medicine, this response shows the medicine info
 * along with all vendors selling it, sorted by cheapest selling price first.
 */
@Data
public class VendorMedicinePriceResponseDTO {
    private Long medicineId;
    private String medicineName;
    private String manufacturer;
    private String saltComposition;
    private String photo1;
    private String packing;
    private String useOf;
    private int totalVendors;
    private List<VendorPriceDTO> vendorPrices;
}