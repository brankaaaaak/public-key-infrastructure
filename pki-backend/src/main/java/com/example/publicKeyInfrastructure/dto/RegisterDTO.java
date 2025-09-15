package com.example.publicKeyInfrastructure.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class RegisterDTO {
    public String name;
    public String lastName;
    public String email;
    public String organization;
    public String password;
}
