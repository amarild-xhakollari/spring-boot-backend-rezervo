package com.myapp.reservations.repository;

import com.myapp.reservations.entities.Review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByBusinessIdOrderByCreatedAtDesc(UUID businessId);

    boolean existsByBusinessIdAndUserId(UUID businessId, UUID userId);
}
