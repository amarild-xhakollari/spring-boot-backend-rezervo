package com.myapp.reservations.controller;

import com.myapp.reservations.dto.timeoffdto.TimeOffRequest;
import com.myapp.reservations.dto.timeoffdto.TimeOffResponse;
import com.myapp.reservations.service.TimeOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-off")
@RequiredArgsConstructor
public class TimeOffController {

    private final TimeOffService timeOffService;

    @PostMapping("/business/{businessId}")
    public ResponseEntity<String> addTimeOff(@PathVariable UUID businessId, @RequestBody TimeOffRequest request) {
        timeOffService.addTimeOff(businessId, request);
        return ResponseEntity.ok("Time off recorded successfully");
    }

    @GetMapping("/business/{businessId}")
    public List<TimeOffResponse> getTimeOffs(@PathVariable UUID businessId) {
        return timeOffService.getBusinessTimeOff(businessId);
    }

    @DeleteMapping("/{timeOffId}")
    public void deleteTimeOff(@PathVariable UUID timeOffId) {
        timeOffService.deleteTimeOff(timeOffId);
    }
}