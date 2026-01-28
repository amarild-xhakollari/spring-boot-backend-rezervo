package com.myapp.reservations.service;

import com.myapp.reservations.dto.timeoffdto.TimeOffRequest;
import com.myapp.reservations.dto.timeoffdto.TimeOffResponse;
import com.myapp.reservations.repository.ScheduleSettingsRepository;
import com.myapp.reservations.repository.TimeOffRepository;
import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import com.myapp.reservations.entities.BusinessSchedule.TimeOff;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeOffService {

    private final TimeOffRepository timeOffRepository;
    private final ScheduleSettingsRepository settingsRepository;

    @Transactional
    public void addTimeOff(UUID businessId, TimeOffRequest request) {
        ScheduleSettings settings = settingsRepository.getScheduleSettingsByBusinessId(businessId)
                .orElseThrow(() -> new RuntimeException("Schedule settings not found"));

        if (request.endDateTime().isBefore(request.startDateTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        TimeOff timeOff = new TimeOff();
        timeOff.setStartDateTime(request.startDateTime());
        timeOff.setEndDateTime(request.endDateTime());
        timeOff.setReason(request.reason());

        timeOff.setScheduleSettings(settings);

        timeOffRepository.save(timeOff);
    }

    public List<TimeOffResponse> getBusinessTimeOff(UUID businessId) {
        return timeOffRepository.findByScheduleSettingsBusinessId(businessId)
                .stream()
                .map(t -> new TimeOffResponse(t.getId(), t.getStartDateTime(), t.getEndDateTime(), t.getReason()))
                .toList();
    }

    public void deleteTimeOff(UUID timeOffId) {
        timeOffRepository.deleteById(timeOffId);
    }
}
