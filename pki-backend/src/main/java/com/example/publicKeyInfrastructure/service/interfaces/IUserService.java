package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.RegisterDTO;
import com.example.publicKeyInfrastructure.dto.UserDTO;

public interface IUserService {
    UserDTO register(RegisterDTO request);
}
