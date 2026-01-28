package com.myapp.reservations.mapper;

import com.myapp.reservations.dto.schedulesettingsdto.ScheduleSettingsRequest;
import com.myapp.reservations.dto.schedulesettingsdto.ScheduleSettingsResponse;
import com.myapp.reservations.dto.workingdaydto.WorkingDayRequest;
import com.myapp.reservations.dto.workingdaydto.WorkingDayResponse;
import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import com.myapp.reservations.entities.BusinessSchedule.WorkingDay;

import java.time.DayOfWeek;
import java.util.List;

public class ScheduleMapper {

    public static ScheduleSettings toScheduleSettings(ScheduleSettingsRequest request) {
        if (request == null) return null;

        ScheduleSettings settings = new ScheduleSettings();
        settings.setReservationType(request.reservationType());
        settings.setSlotDurationValue(request.slotDurationValue());
        settings.setSlotDurationUnit(request.slotDurationUnit());
        settings.setMinAdvanceBookingHours(request.minAdvanceBookingHours());
        settings.setMaxAdvanceBookingDays(request.maxAdvanceBookingDays());
        settings.setAutoConfirmAppointments(request.autoConfirmAppointments());

        if (request.workingDays() != null) {
            List<WorkingDay> days = request.workingDays().stream()
                    .map(dayRequest -> toWorkingDay(dayRequest, settings))
                    .toList();
            settings.setWorkingDays(days);
        }

        return settings;
    }

    public static ScheduleSettingsResponse toResponse(ScheduleSettings entity) {
        if (entity == null) return null;

        List<WorkingDayResponse> dayResponses = entity.getWorkingDays() != null
                ? entity.getWorkingDays().stream()
                .map(ScheduleMapper::toWorkingDayResponse)
                .toList()
                : List.of();

        return new ScheduleSettingsResponse(
                entity.getId(),
                entity.getReservationType(),
                entity.getSlotDurationValue(),
                entity.getSlotDurationUnit(),
                entity.getMinAdvanceBookingHours(),
                entity.getMaxAdvanceBookingDays(),
                entity.getAutoConfirmAppointments(),
                dayResponses
        );
    }

    private static WorkingDay toWorkingDay(WorkingDayRequest request, ScheduleSettings settings) {
        WorkingDay day = new WorkingDay();
        day.setDayOfWeek(DayOfWeek.valueOf(request.dayOfWeek().toUpperCase()));
        day.setStartTime(request.startTime());
        day.setEndTime(request.endTime());
        day.setBreakStartTime(request.breakStartTime());
        day.setBreakEndTime(request.breakEndTime());
        day.setDayOff(request.isDayOff());
        day.setScheduleSettings(settings);
        return day;
    }

    private static WorkingDayResponse toWorkingDayResponse(WorkingDay entity) {
        return new WorkingDayResponse(
                entity.getId(),
                entity.getDayOfWeek().name(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getBreakStartTime(),
                entity.getBreakEndTime(),
                entity.isDayOff()
        );
    }
}
