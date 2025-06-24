package com.trego.service;

import com.trego.dao.entity.Address;
import com.trego.dto.AddressDTO;

import java.util.List;
import java.util.Optional;

public interface IAddressService {


    AddressDTO createAddress(AddressDTO addressDTO);

    Optional<Address> getAddressById(Long id);

    List<AddressDTO> getAddressesByUserId(Long userId);

    Address updateAddress(Long id, Address address);

    void deleteAddress(Long id);
}
