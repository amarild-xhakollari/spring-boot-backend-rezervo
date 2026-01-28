package com.myapp.reservations.controller;

import com.myapp.reservations.dto.businessphotodto.BusinessPhotoResponse;
import com.myapp.reservations.service.FileStorageService;
import com.myapp.reservations.service.UserService;
import com.myapp.reservations.service.BusinessService;
import com.myapp.reservations.service.BusinessPhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final BusinessService businessService;
    private final BusinessPhotoService businessPhotoService;

    public FileController(FileStorageService fileStorageService,
                         UserService userService,
                         BusinessService businessService,
                         BusinessPhotoService businessPhotoService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.businessService = businessService;
        this.businessPhotoService = businessPhotoService;
    }

    @PostMapping("/user-avatar")
    public ResponseEntity<?> uploadUserAvatar(@RequestParam("file") MultipartFile file) {
        try {
            UUID userId = userService.getCurrentUserId();
            String filePath = fileStorageService.storeFile(file, "avatars");
            userService.updateAvatar(userId, filePath);
            return ResponseEntity.ok(Map.of(
                "path", filePath,
                "url", "/uploads/" + filePath
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @PostMapping("/business-image/{businessId}")
    public ResponseEntity<?> uploadBusinessImage(
            @PathVariable UUID businessId,
            @RequestParam("file") MultipartFile file) {
        try {
            UUID userId = userService.getCurrentUserId();
            if (!businessService.isOwnerOrAdmin(businessId, userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            String filePath = fileStorageService.storeFile(file, "businesses");
            businessService.updateImage(businessId, filePath);
            return ResponseEntity.ok(Map.of(
                "path", filePath,
                "url", "/uploads/" + filePath
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @DeleteMapping("/user-avatar")
    public ResponseEntity<?> deleteUserAvatar() {
        try {
            UUID userId = userService.getCurrentUserId();
            String currentPath = userService.getAvatarPath(userId);
            if (currentPath != null) {
                fileStorageService.deleteFile(currentPath);
                userService.updateAvatar(userId, null);
            }
            return ResponseEntity.ok(Map.of("message", "Avatar deleted"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete file"));
        }
    }

    @DeleteMapping("/business-image/{businessId}")
    public ResponseEntity<?> deleteBusinessImage(@PathVariable UUID businessId) {
        try {
            UUID userId = userService.getCurrentUserId();
            if (!businessService.isOwnerOrAdmin(businessId, userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            String currentPath = businessService.getImagePath(businessId);
            if (currentPath != null) {
                fileStorageService.deleteFile(currentPath);
                businessService.updateImage(businessId, null);
            }
            return ResponseEntity.ok(Map.of("message", "Image deleted"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete file"));
        }
    }

    @GetMapping("/business-photos/{businessId}")
    public ResponseEntity<List<BusinessPhotoResponse>> getBusinessPhotos(@PathVariable UUID businessId) {
        List<BusinessPhotoResponse> photos = businessPhotoService.getPhotosByBusiness(businessId);
        return ResponseEntity.ok(photos);
    }

    @PostMapping("/business-photos/{businessId}")
    public ResponseEntity<?> uploadBusinessPhoto(
            @PathVariable UUID businessId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {
        try {
            String filePath = fileStorageService.storeFile(file, "gallery/" + businessId);
            BusinessPhotoResponse photo = businessPhotoService.addPhoto(businessId, filePath, caption);
            return ResponseEntity.ok(photo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @DeleteMapping("/business-photos/{photoId}")
    public ResponseEntity<?> deleteBusinessPhoto(@PathVariable UUID photoId) {
        try {
            businessPhotoService.deletePhoto(photoId);
            return ResponseEntity.ok(Map.of("message", "Photo deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete file"));
        }
    }

    @PatchMapping("/business-photos/{photoId}/caption")
    public ResponseEntity<?> updatePhotoCaption(
            @PathVariable UUID photoId,
            @RequestBody Map<String, String> body) {
        try {
            String caption = body.get("caption");
            BusinessPhotoResponse photo = businessPhotoService.updateCaption(photoId, caption);
            return ResponseEntity.ok(photo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
