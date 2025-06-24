package com.trego.dto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String address;
    private String city;
    private String landmark;
    private String pincode;
    private String mobileNo;
    private String name;
    private int addressType;
    private Double lat;
    private Double lng;
    private long userId;


    public AddressDTO(Long id, String address, String city, String landmark, String pincode, Double lat, Double lng, long userId, String mobileNo, String name, int addressType) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.landmark = landmark;
        this.pincode = pincode;
        this.lat = lat;
        this.lng = lng;
        this.userId = userId;
        this.mobileNo = mobileNo;
        this.name = name;
        this.addressType = addressType;
    }
}
