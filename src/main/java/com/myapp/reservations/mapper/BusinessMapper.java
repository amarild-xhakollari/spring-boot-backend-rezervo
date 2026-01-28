package com.myapp.reservations.mapper;

import com.myapp.reservations.dto.businessdto.BusinessRequest;
import com.myapp.reservations.dto.businessdto.BusinessResponse;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.businessentity.BusinessType;
import com.myapp.reservations.entities.User.User;

import java.util.ArrayList;
import java.util.List;

public class BusinessMapper {

    public static BusinessResponse toResponse(Business business) {
        if (business == null) return null;

        String category = getDisplayCategory(business.getBusinessType(), business.getCustomType());
        String imageUrl = business.getImagePath() != null
                ? "/uploads/" + business.getImagePath()
                : null;

        return new BusinessResponse(
                business.getId(),
                business.getName(),
                business.getDescription(),
                business.getAddress(),
                business.getPhone(),
                business.getOwner().getId(),
                business.getAdmins().stream()
                        .map(User::getId)
                        .toList(),
                ScheduleMapper.toResponse(business.getScheduleSettings()),
                business.getOfferings().stream()
                        .map(OfferingMapper::toResponse)
                        .toList(),
                business.getBusinessType(),
                business.getCustomType(),
                category,
                imageUrl
        );
    }

    public static Business toBusiness(BusinessRequest request, User owner, List<User> admins) {
        if (request == null) return null;

        Business business = new Business();
        business.setName(request.name());
        business.setDescription(request.description());
        business.setAddress(request.address());
        business.setPhone(request.phone());
        business.setBusinessType(request.businessType());
        business.setCustomType(request.customType());
        business.setOwner(owner);
        business.setAdmins(admins != null ? new ArrayList<>(admins) : new ArrayList<>());
        return business;
    }

    public static Business toBusiness(BusinessRequest request, User owner) {
        return toBusiness(request, owner, new ArrayList<>());
    }

    private static String getDisplayCategory(BusinessType type, String customType) {
        if (type == null) return null;
        return switch (type) {
            case SPA_WELLNESS -> "Spa & Wellness";
            case BARBERSHOP -> "Barbershop";
            case BEAUTY_SALON -> "Beauty Salon";
            case FITNESS -> "Fitness";
            case YOGA_MEDITATION -> "Yoga & Meditation";
            case PET_SERVICES -> "Pet Services";
            case OTHER -> customType != null ? customType : "Other";
        };
    }
}
