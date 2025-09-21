package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.service.interfaces.IOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
@CrossOrigin(origins = "https://localhost:4200")
public class OrganizationController {

    @Autowired
    IOrganizationService service;

    @GetMapping("/names")
    public List<String> getOrganizationNames() {
        return service.getAllOrganizationNames();
    }
}
