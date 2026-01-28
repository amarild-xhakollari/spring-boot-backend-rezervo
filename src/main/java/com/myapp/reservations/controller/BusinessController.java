package com.myapp.reservations.controller;

import com.myapp.reservations.dto.businessdto.BusinessRequest;
import com.myapp.reservations.dto.businessdto.BusinessResponse;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.service.BusinessService;
import com.myapp.reservations.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {
    private final BusinessService businessService;
    private final UserService userService;

    public BusinessController(BusinessService businessService , UserService userService) {
        this.businessService = businessService;
        this.userService = userService;
    }

    @GetMapping("")
    public List<BusinessResponse> getAllBusinesses() {
        return businessService.getAllBusinesses();
    }

    @GetMapping("/by-business-id/{id}")
    public BusinessResponse getBusinessById(@PathVariable UUID id) {
        return businessService.getBusinessById(id);
    }

    @GetMapping("/by-business-name/{name}")
    public BusinessResponse getBusinessByName(@PathVariable(value = "name") String b_name) {
        return businessService.getBusinessByName(b_name);
    }

    @GetMapping("/by-business-owners-id/{owner_id}")
    public List<BusinessResponse> getBusinessesByUserId(@PathVariable(value = "owner_id") UUID id) {
        return businessService.getAllBusinessesByUserId(id);
    }

    @PostMapping("/create")
    public BusinessResponse createBusiness(@Valid @RequestBody BusinessRequest business) {
        UUID currentUserId = userService.getCurrentUserId();
        return businessService.createBusiness(business, currentUserId);
    }

    @PutMapping("/update/{id}")
    public BusinessResponse update(@PathVariable UUID id, @RequestBody BusinessRequest business) {
        if (business == null || id == null) return null ;
        return businessService.updateBusiness(id, business);
    }

    @DeleteMapping("/{id}")
    public void deleteBusinessById(@PathVariable UUID id) {
        businessService.deleteBusinessById(id);
    }

    @GetMapping("{businessId}/admins")
    public List<UserResponse> getAllAdmins(@PathVariable UUID businessId){
        if (businessId == null ) return null ;
        return businessService.getAllAdmins(businessId);
    }

    @PutMapping("{businessId}/admins")
    public void addAdminToBusiness(@PathVariable UUID businessId, @RequestBody UUID userId){
        businessService.addAdminToBusiness(businessId,userId);
    }

}
