package com.myapp.reservations.service;

import com.myapp.reservations.dto.businessdto.BusinessRequest;
import com.myapp.reservations.dto.businessdto.BusinessResponse;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.exception.notfoundexceptions.BusinessNotFoundException;
import com.myapp.reservations.exception.notfoundexceptions.UserNotFoundException;
import com.myapp.reservations.mapper.BusinessMapper;
import com.myapp.reservations.mapper.UserMapper;
import com.myapp.reservations.repository.BusinessRepository;
import com.myapp.reservations.repository.UserRepository;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.user.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessService {
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    public BusinessService(BusinessRepository businessRepository, UserRepository userRepository, ScheduleService scheduleService) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.scheduleService=scheduleService;
    }

    public BusinessResponse getBusinessById(UUID businessId) {
        if(businessId == null) {
            throw new IllegalArgumentException("Id not provided") ;
        }
        Optional<Business> business = businessRepository.findById(businessId);
        return business.map(BusinessMapper::toResponse).orElseThrow(() -> new BusinessNotFoundException( businessId));
    }

    public List<BusinessResponse> getAllBusinesses() {
        List<Business> businesses = businessRepository.findAll();
         return businesses.stream().map(BusinessMapper::toResponse).toList();
    }

    public BusinessResponse getBusinessByName(String name) {
        if(name == null) {
            throw new IllegalArgumentException("BusinessName not provided");
        }
        Optional<Business> business = businessRepository.getBusinessByName(name);
        return business.map(BusinessMapper::toResponse).orElseThrow(() -> new BusinessNotFoundException(name));
    }

    public List<BusinessResponse> getAllBusinessesByUserId(UUID ownerId) {
        if(ownerId == null) {
            throw new IllegalArgumentException("OwnerId not provided");
        }
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        List<Business> businesses = businessRepository.getAllBusinessByUserId(user.getId());
        return businesses.stream().map(BusinessMapper::toResponse).toList();
    }

    public void deleteBusinessById(UUID businessId) {

        if(businessId == null) {
            throw new IllegalArgumentException("BusinessId not provided");
        }

        if(!businessRepository.existsById(businessId)){
            throw new BusinessNotFoundException(businessId);
        }
        businessRepository.deleteById(businessId);
    }

    @Transactional
    public BusinessResponse createBusiness(BusinessRequest request, UUID currentUserId) {
        if (request == null) {
            throw new IllegalArgumentException("BusinessRequest not provided");
        }
        if(currentUserId == null){
            throw new IllegalArgumentException("CurrentUserId not provided");
        }
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));

        Business business = BusinessMapper.toBusiness(request, owner);
        scheduleService.createDefaultSchedule(business);
        Business savedBusiness = businessRepository.save(business);
        if(!owner.getRoles().contains("BUSINESS_OWNER")) {
            owner.getRoles().add("BUSINESS_OWNER");
            userRepository.save(owner);
        }
        return BusinessMapper.toResponse(savedBusiness);
    }

    @Transactional
    public BusinessResponse updateBusiness(UUID businessId, BusinessRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("BusinessRequest not provided");
        }
        if(businessId == null){
            throw new IllegalArgumentException("BusinessId not provided");
        }

        Business existing = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        if (request.name() != null) existing.setName(request.name());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.address() != null) existing.setAddress(request.address());
        if (request.phone() != null) existing.setPhone(request.phone());
        if (request.businessType() != null) existing.setBusinessType(request.businessType());
        if (request.customType() != null) existing.setCustomType(request.customType());

        Business saved = businessRepository.save(existing);

        return BusinessMapper.toResponse(saved);
    }

    @Transactional
    public void addAdminToBusiness(UUID businessId, UUID userId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        business.getAdmins().add(user);
        user.getAdminOfBusinesses().add(business);
        if(!user.getRoles().contains("BUSINESS_ADMIN")) {
            user.getRoles().add("BUSINESS_ADMIN");
        }
        userRepository.save(user);
        businessRepository.save(business);
    }

    public List<UserResponse> getAllAdmins(UUID businessId){
        Business business = businessRepository.getBusinessById(businessId).orElseThrow(() -> new BusinessNotFoundException(businessId));
        return business.getAdmins().stream().map(UserMapper::toResponse).toList();
    }

    public boolean isOwnerOrAdmin(UUID businessId, UUID userId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        if (business.getOwner().getId().equals(userId)) {
            return true;
        }

        return business.getAdmins().stream()
                .anyMatch(admin -> admin.getId().equals(userId));
    }

    @Transactional
    public void updateImage(UUID businessId, String imagePath) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
        business.setImagePath(imagePath);
        businessRepository.save(business);
    }

    public String getImagePath(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
        return business.getImagePath();
    }
}
