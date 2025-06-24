package com.trego.service;

import com.trego.dao.entity.User;
import com.trego.dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface IUserService {

 UserDTO createUser(UserDTO userDTO);

 UserDTO getUserById(Long id);

 List<User> getAllUsers();

 User updateUser(Long id, User user);

 void deleteUser(Long id);
}
