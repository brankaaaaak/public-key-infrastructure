package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.*;
import jakarta.validation.Valid;

import java.util.List;

public interface IUserService {
    UserDTO register(RegisterDTO request);
    public TokenPair login(LoginDTO request);
    public TokenPair refreshToken(@Valid RefreshTokenDTO request);

    List<UserViewDTO> getAllUsers();
}
