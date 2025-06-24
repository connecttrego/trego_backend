package com.trego.dao.entity;


import com.trego.enums.AddressType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;



@Data
@Entity(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String city;
    private String landmark;
    private String pincode;
    private Double lat;
    private Double lng;

    @Column(name = "mobile_no")
    private String mobileNo;
    private String name;

    @Column(nullable = true, name = "address_type")
    private int addressType;


/*    @Column(name = "address_type")
    private String addressType;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // Foreign key column referencing User
    private User user;  // Many-to-one relationship with User

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", landmark='" + landmark + '\'' +
                ", pincode='" + pincode + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", mobileNo='" + mobileNo + '\'' +
                ", name='" + name + '\'' +
                ", addressType='" + addressType + '\'' +
                ", user=" + user +
                '}';
    }

}
