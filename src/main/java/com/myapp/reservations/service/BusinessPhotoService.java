package com.myapp.reservations.service;

import com.myapp.reservations.dto.businessphotodto.BusinessPhotoResponse;
import com.myapp.reservations.repository.BusinessPhotoRepository;
import com.myapp.reservations.repository.BusinessRepository;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.businessentity.BusinessPhoto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusinessPhotoService {

    private final BusinessPhotoRepository photoRepository;
    private final BusinessRepository businessRepository;
    private final FileStorageService fileStorageService;
    private final BusinessService businessService;
    private final UserService userService;

    private static final int MAX_PHOTOS_PER_BUSINESS = 20;

    public BusinessPhotoService(BusinessPhotoRepository photoRepository,
                                BusinessRepository businessRepository,
                                FileStorageService fileStorageService,
                                BusinessService businessService,
                                UserService userService) {
        this.photoRepository = photoRepository;
        this.businessRepository = businessRepository;
        this.fileStorageService = fileStorageService;
        this.businessService = businessService;
        this.userService = userService;
    }

    public List<BusinessPhotoResponse> getPhotosByBusiness(UUID businessId) {
        return photoRepository.findByBusinessIdOrderByDisplayOrderAscCreatedAtDesc(businessId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessPhotoResponse addPhoto(UUID businessId, String filePath, String caption) {
        UUID userId = userService.getCurrentUserId();

        if (!businessService.isOwnerOrAdmin(businessId, userId)) {
            throw new RuntimeException("Not authorized to add photos to this business");
        }

        long photoCount = photoRepository.countByBusinessId(businessId);
        if (photoCount >= MAX_PHOTOS_PER_BUSINESS) {
            throw new RuntimeException("Maximum number of photos (" + MAX_PHOTOS_PER_BUSINESS + ") reached");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        Integer maxOrder = photoRepository.findMaxDisplayOrderByBusinessId(businessId);

        BusinessPhoto photo = new BusinessPhoto();
        photo.setBusiness(business);
        photo.setFilePath(filePath);
        photo.setCaption(caption);
        photo.setDisplayOrder(maxOrder + 1);

        photoRepository.save(photo);

        return toResponse(photo);
    }

    @Transactional
    public void deletePhoto(UUID photoId) throws IOException {
        UUID userId = userService.getCurrentUserId();

        BusinessPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        if (!businessService.isOwnerOrAdmin(photo.getBusiness().getId(), userId)) {
            throw new RuntimeException("Not authorized to delete this photo");
        }

        fileStorageService.deleteFile(photo.getFilePath());

        photoRepository.delete(photo);
    }

    @Transactional
    public BusinessPhotoResponse updateCaption(UUID photoId, String caption) {
        UUID userId = userService.getCurrentUserId();

        BusinessPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        if (!businessService.isOwnerOrAdmin(photo.getBusiness().getId(), userId)) {
            throw new RuntimeException("Not authorized to update this photo");
        }

        photo.setCaption(caption);
        photoRepository.save(photo);

        return toResponse(photo);
    }

    private BusinessPhotoResponse toResponse(BusinessPhoto photo) {
        return new BusinessPhotoResponse(
                photo.getId(),
                "/uploads/" + photo.getFilePath(),
                photo.getCaption(),
                photo.getDisplayOrder(),
                photo.getCreatedAt()
        );
    }
}
