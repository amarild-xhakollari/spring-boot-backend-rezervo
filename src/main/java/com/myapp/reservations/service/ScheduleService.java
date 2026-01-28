package com.myapp.reservations.service;

import com.myapp.reservations.dto.schedulesettingsdto.ScheduleSettingsRequest;
import com.myapp.reservations.dto.schedulesettingsdto.ScheduleSettingsResponse;
import com.myapp.reservations.dto.workingdaydto.WorkingDayRequest;
import com.myapp.reservations.mapper.ScheduleMapper;
import com.myapp.reservations.repository.BusinessRepository;
import com.myapp.reservations.repository.ScheduleSettingsRepository;
import com.myapp.reservations.repository.WorkingDayRepository;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.Reservation.ReservationType;
import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import com.myapp.reservations.entities.BusinessSchedule.WorkingDay;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class ScheduleService {

    private final ScheduleSettingsRepository scheduleSettingsRepository;
    private final WorkingDayRepository workingDayRepository;
    private final BusinessRepository businessRepository;

    public ScheduleService(ScheduleSettingsRepository scheduleSettingsRepository, WorkingDayRepository workingDayRepository, BusinessRepository businessRepository){
        this.scheduleSettingsRepository=scheduleSettingsRepository;
        this.workingDayRepository=workingDayRepository;
        this.businessRepository = businessRepository;
    }

    public void createDefaultSchedule(Business business) {
        ScheduleSettings settings = new ScheduleSettings();
        settings.setSlotDurationValue(30);
        settings.setSlotDurationUnit(ChronoUnit.MINUTES);
        settings.setReservationType(ReservationType.SLOT);
        settings.setAutoConfirmAppointments(true);
        settings.setMinAdvanceBookingHours(2);
        settings.setMaxAdvanceBookingDays(30);

        settings.setBusiness(business);

        List<WorkingDay> days = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkingDay wd = new WorkingDay();
            wd.setDayOfWeek(day);
            wd.setStartTime(LocalTime.of(9, 0));
            wd.setEndTime(LocalTime.of(17, 0));

            wd.setDayOff(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);

            wd.setScheduleSettings(settings);
            days.add(wd);
        }

        settings.setWorkingDays(days);

        business.setScheduleSettings(settings);
    }

    @Transactional
    public void updateSchedule(UUID businessId, ScheduleSettingsRequest request) {
        if (businessId == null) return;

        ScheduleSettings existing = scheduleSettingsRepository.getScheduleSettingsByBusinessId(businessId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));


        if (request.reservationType() != null) {
            existing.setReservationType(request.reservationType());
        }
        if (request.slotDurationValue() != null) {
            existing.setSlotDurationValue(request.slotDurationValue());
        }
        if (request.slotDurationUnit() != null) {
            existing.setSlotDurationUnit(request.slotDurationUnit());
        }
        if (request.minAdvanceBookingHours() != null) {
            existing.setMinAdvanceBookingHours(request.minAdvanceBookingHours());
        }
        if (request.maxAdvanceBookingDays() != null) {
            existing.setMaxAdvanceBookingDays(request.maxAdvanceBookingDays());
        }
        if (request.autoConfirmAppointments() != null) {
            existing.setAutoConfirmAppointments(request.autoConfirmAppointments());
        }

        for (WorkingDayRequest dayReq : request.workingDays()) {
            WorkingDay existingDay = workingDayRepository
                    .findByScheduleSettingsIdAndDayOfWeek(existing.getId(), DayOfWeek.valueOf(dayReq.dayOfWeek()));

            updateDayDetails(existingDay, dayReq);
        }

        scheduleSettingsRepository.save(existing);
    }

    private void updateDayDetails(WorkingDay entity, WorkingDayRequest req) {
        entity.setStartTime(req.startTime());
        entity.setEndTime(req.endTime());
        entity.setBreakStartTime(req.breakStartTime());
        entity.setBreakEndTime(req.breakEndTime());
        entity.setDayOff(req.isDayOff());

        if (!entity.isDayOff()) {
            validateWorkingHours(entity);
        }
    }

    private void validateWorkingHours(WorkingDay day) {
        if (day.getStartTime().isAfter(day.getEndTime())) {
            throw new IllegalArgumentException("Opening time must be before closing time for " + day.getDayOfWeek());
        }

        if (day.getBreakStartTime() != null && day.getBreakEndTime() != null) {
            if (day.getBreakStartTime().isAfter(day.getBreakEndTime())) {
                throw new IllegalArgumentException("Break start must be before break end for " + day.getDayOfWeek());
            }

            boolean isInside = day.getBreakStartTime().isAfter(day.getStartTime()) &&
                    day.getBreakEndTime().isBefore(day.getEndTime());

            if (!isInside) {
                throw new IllegalArgumentException("Break must be within working hours for " + day.getDayOfWeek());
            }
        }
    }

    public ScheduleSettingsResponse getScheduleById(UUID scheduleId){
        return ScheduleMapper.toResponse(scheduleSettingsRepository.getScheduleSettingsById(scheduleId));

    }

    public ScheduleSettingsResponse getScheduleByBusinessId(UUID businessId){
        if(businessId ==null){
            return null;
        }
        Business business = businessRepository.getBusinessById(businessId).orElseThrow(()-> new RuntimeException("Business not found"));

        return ScheduleMapper.toResponse(business.getScheduleSettings());

    }

    public List<ScheduleSettingsResponse> getAllSchedules(){
        return scheduleSettingsRepository.findAll().stream().map(ScheduleMapper::toResponse).toList();
    }


}
