package app.service;

import app.dto.CreateUserRequest;
import app.dto.UpdateUserRequest;
import app.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO createUser(CreateUserRequest request);
    UserDTO updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
}
