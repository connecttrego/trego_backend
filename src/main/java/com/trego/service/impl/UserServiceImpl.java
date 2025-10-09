package com.trego.service.impl;

import com.trego.dao.entity.User;
import com.trego.dao.impl.UserRepository;
import com.trego.dto.AddressDTO;
import com.trego.dto.UserDTO;
import com.trego.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User existingUser =  userRepository.findByEmail(userDTO.getEmail());
        if(existingUser == null ){
           User user = new User();
           user.setName(userDTO.getName());
           user.setPassword(userDTO.getPassword());
           user.setEmail(userDTO.getEmail());
           user.setRole(userDTO.getRole());
           user = userRepository.save(user);
           userDTO.setId(user.getId());
       }else{
           userDTO.setId(existingUser.getId());
            userDTO.setName(existingUser.getName());
            userDTO.setPassword(existingUser.getPassword());
            userDTO.setEmail(existingUser.getEmail());
            userDTO.setRole(existingUser.getRole());
       }
        return userDTO;
    }


    @Override
    public UserDTO getUserById(Long id) {
        UserDTO userDTO = new UserDTO();
        User user= userRepository.findById(id).get();
        userDTO.setId(user.getId());
        userDTO.setRole(user.getRole());
        userDTO.setMobile(user.getMobile());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());

        List<AddressDTO> addressDTOS   =  user.getAddresses().stream()
                .map(address -> new AddressDTO(
                        address.getId(),
                        address.getAddress(),
                        address.getCity(),
                        address.getLandmark(),
                        address.getPincode(),
                        address.getLat(),
                        address.getLng(), address.getUser().getId(), address.getMobileNo(), address.getName(), address.getAddressTypeValue()))
                .collect(Collectors.toList());
        userDTO.setAddress(addressDTOS);
        return userDTO;
    }


    @Override
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }


    @Override
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setPasswordHint(userDetails.getPasswordHint());
        user.setUpdatedAt(userDetails.getUpdatedAt());
        return userRepository.save(user);
    }


    @Override
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }
}
