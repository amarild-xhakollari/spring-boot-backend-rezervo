package com.myapp.reservations.controller;

import com.myapp.reservations.dto.reservationdto.RejectRequest;
import com.myapp.reservations.dto.reservationdto.ReservationRequest;
import com.myapp.reservations.dto.reservationdto.ReservationResponse;
import com.myapp.reservations.service.ReservationService;
import com.myapp.reservations.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }



    @PostMapping("/create")
    public ReservationResponse createReservation(@RequestBody  ReservationRequest request){
        return reservationService.createReservation(request);

    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public List<ReservationResponse> getMyReservations() {
        UUID currentUserId = userService.getCurrentUserId();
        return reservationService.getMyReservations(currentUserId);
    }

    @GetMapping("/business/{businessId}")
    public List<ReservationResponse> getReservationsByBusiness(@PathVariable UUID businessId) {
        return reservationService.getReservationsByBusiness(businessId);
    }

    @PatchMapping("/{id}/confirm")
    public ReservationResponse confirmReservation(@PathVariable UUID id) {
        return reservationService.confirmReservation(id);
    }

    @PatchMapping("/{id}/reject")
    public ReservationResponse rejectReservation(@PathVariable UUID id, @RequestBody(required = false) RejectRequest request) {
        String reason = request != null ? request.reason() : null;
        return reservationService.rejectReservation(id, reason);
    }
}