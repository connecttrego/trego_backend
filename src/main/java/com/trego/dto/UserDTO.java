package com.trego.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {

    private long id;
    private String name;
    private String email;
    private String role;
    private String password;
    private long mobile;
   List<AddressDTO> address;
}
