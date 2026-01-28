package com.myapp.reservations.controller;

import com.myapp.reservations.dto.businessdto.BusinessRequest;
import com.myapp.reservations.dto.businessdto.BusinessResponse;
import com.myapp.reservations.dto.userdto.ProfileUpdateRequest;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.service.BusinessService;
import com.myapp.reservations.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/me")
@PreAuthorize("hasAnyRole('USER','BUSINESS_OWNER','BUSINESS_ADMIN','ADMIN')")
public class UserSelfController {

    private final UserService userService;
    private final BusinessService businessService;

    @Autowired
    public UserSelfController(UserService userService, BusinessService businessService) {
        this.userService = userService;
        this.businessService = businessService;
    }

    @GetMapping
    public UserResponse getMyProfile() {
        UUID currentUserId = userService.getCurrentUserId();
        return userService.findById(currentUserId);
    }

    @PutMapping
    public void updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        UUID currentUserId = userService.getCurrentUserId();
        userService.updateProfile(currentUserId, request);
    }

    @DeleteMapping
    public void deleteMyProficreatele() {
        UUID currentUserId = userService.getCurrentUserId();
        userService.deleteUserById(currentUserId);
    }

    @PostMapping("/business")
    public BusinessResponse createMyBusiness(@Valid @RequestBody BusinessRequest request) {
        UUID currentUserId = userService.getCurrentUserId();
        return businessService.createBusiness(request, currentUserId);
    }

    @GetMapping("/businesses")
    public List<BusinessResponse> myBusinesses() {
        UUID currentUserId = userService.getCurrentUserId();
        return businessService.getAllBusinessesByUserId(currentUserId);
    }


}

