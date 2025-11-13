package com.trego.service.impl;

import com.trego.dao.entity.Address;
import com.trego.dao.entity.User;
import com.trego.dao.impl.AddressRepository;
import com.trego.dto.AddressDTO;
import com.trego.enums.AddressType;
import com.trego.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements IAddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {

        Address address = new Address();
        address.setAddress(addressDTO.getAddress());
        address.setCity(addressDTO.getCity());
        address.setLandmark(addressDTO.getLandmark());
        address.setPincode(addressDTO.getPincode());
        address.setLat(addressDTO.getLat());
        address.setLng(addressDTO.getLng());
        address.setMobileNo(addressDTO.getMobileNo());
        address.setName(addressDTO.getName());
        // Convert int to AddressType enum
        address.setAddressType(AddressType.fromCode(addressDTO.getAddressType()));
        User user  = new User();
        user.setId(addressDTO.getUserId());
        address.setUser(user);
        addressDTO.setId(addressRepository.save(address).getId());
        return addressDTO;
    }


    @Override
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }


    @Override
    public List<AddressDTO> getAddressesByUserId(Long userId) {

        List<AddressDTO> addressDTOs = populateAddressDTO(addressRepository.findByUserId(userId));
        return addressDTOs;
    }

    private List<AddressDTO> populateAddressDTO(List<Address> addressList) {
        List<AddressDTO> addressDTOS = new ArrayList<>();

        return addressList.stream()
                .map(address -> new AddressDTO(
                        address.getId(),
                        address.getAddress(),
                        address.getCity(),
                        address.getLandmark(),
                        address.getPincode(),
                        address.getLat(),
                        address.getLng(), address.getUser().getId() , address.getMobileNo() , address.getName(), address.getAddressType().getCode()))
                .collect(Collectors.toList());

    }


    @Override
    public Address updateAddress(Long id, Address addressDetails) {
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        address.setAddress(addressDetails.getAddress());
        address.setCity(addressDetails.getCity());
        address.setLandmark(addressDetails.getLandmark());
        address.setPincode(addressDetails.getPincode());
        address.setLat(addressDetails.getLat());
        address.setLng(addressDetails.getLng());
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}