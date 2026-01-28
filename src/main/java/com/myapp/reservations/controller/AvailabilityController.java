package com.myapp.reservations.controller;

import com.myapp.reservations.dto.BusyBlockResponse;
import com.myapp.reservations.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService){
        this.availabilityService=availabilityService;
    }

    @GetMapping("/busy-blocks/{businessId}")
    public  List<BusyBlockResponse> getBusyBlocks(@PathVariable UUID businessId,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return availabilityService.getBusyBlocks(businessId,start,end);

    }
}
