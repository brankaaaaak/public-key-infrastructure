package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.*;
import com.example.publicKeyInfrastructure.model.UserWithRoleDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface IUserService {
    UserDTO register(RegisterDTO request);
    public TokenPair login(LoginDTO request);
    public TokenPair refreshToken(@Valid RefreshTokenDTO request);

    List<UserWithRoleDTO> getAllUsers();
    UserDTO getUserById(Long id);

    List<UserTableViewDTO> getUsersForOrganization(Long id);
    public List<UserTableViewDTO> getAllUsersForOrganization(Long id);
    public List<UserTableViewDTO> getAllCAUsers() ;
}
