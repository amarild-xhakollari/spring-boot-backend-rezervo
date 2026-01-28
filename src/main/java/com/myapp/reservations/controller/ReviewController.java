package com.myapp.reservations.controller;

import com.myapp.reservations.dto.reviewdto.ReviewRequest;
import com.myapp.reservations.dto.reviewdto.ReviewResponse;
import com.myapp.reservations.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{businessId}")
    public List<ReviewResponse> getReviewsByBusiness(@PathVariable UUID businessId) {
        return reviewService.getReviewsByBusiness(businessId);
    }

    @PostMapping("/{businessId}")
    public ReviewResponse createReview(@PathVariable UUID businessId, @Valid @RequestBody ReviewRequest request) {
        return reviewService.createReview(businessId, request);
    }
}
