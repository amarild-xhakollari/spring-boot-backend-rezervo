package com.myapp.reservations.service;

import com.myapp.reservations.dto.BusyBlockResponse;
import com.myapp.reservations.repository.ReservationRepository;
import com.myapp.reservations.repository.ScheduleSettingsRepository;
import com.myapp.reservations.repository.TimeOffRepository;
import com.myapp.reservations.entities.Reservation.Reservation;
import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import com.myapp.reservations.entities.BusinessSchedule.TimeOff;
import com.myapp.reservations.entities.BusinessSchedule.WorkingDay;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private final ReservationRepository reservationRepository;
    private final ScheduleSettingsRepository scheduleSettingsRepository;
    private final TimeOffRepository timeOffRepository;

    public AvailabilityService(ReservationRepository reservationRepository , ScheduleSettingsRepository scheduleSettingsRepository, TimeOffRepository timeOffRepository){
        this.scheduleSettingsRepository= scheduleSettingsRepository;
        this.reservationRepository =reservationRepository;
        this.timeOffRepository = timeOffRepository;
    }

    public List<BusyBlockResponse> getBusyBlocks(UUID businessId, LocalDateTime viewStart, LocalDateTime viewEnd) {
        List<BusyBlockResponse> busyBlocks = new ArrayList<>();

        ScheduleSettings settings = scheduleSettingsRepository.getScheduleSettingsByBusinessId(businessId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        busyBlocks.addAll(calculateClosedBlocks(settings, viewStart, viewEnd));

        List<Reservation> reservations = reservationRepository.findActiveByBusinessInRange(businessId, viewStart, viewEnd);
        for (Reservation res : reservations) {
            busyBlocks.add(new BusyBlockResponse(res.getStartDateTime(), res.getEndDateTime(), "OCCUPIED"));
        }

        List<TimeOff> TimeOffs = timeOffRepository.findByBusinessIdAndRange(businessId, viewStart, viewEnd);
        for (TimeOff timeOff : TimeOffs) {
            busyBlocks.add(new BusyBlockResponse(timeOff.getStartDateTime(), timeOff.getEndDateTime(), "OCCUPIED"));
        }

        busyBlocks.sort(Comparator.comparing(BusyBlockResponse::start));

        return busyBlocks;
    }

    private List<BusyBlockResponse> calculateClosedBlocks(ScheduleSettings settings, LocalDateTime start, LocalDateTime end) {
        List<BusyBlockResponse> closedBlocks = new ArrayList<>();

        for (LocalDate date = start.toLocalDate(); !date.isAfter(end.toLocalDate()); date = date.plusDays(1)) {
            final DayOfWeek dow = date.getDayOfWeek();
            WorkingDay dayConfig = settings.getWorkingDays().stream()
                    .filter(wd -> wd.getDayOfWeek().equals(dow))
                    .findFirst().orElse(null);

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            if (dayConfig == null || dayConfig.isDayOff()) {
                closedBlocks.add(new BusyBlockResponse(startOfDay, endOfDay, "CLOSED"));
            } else {
                closedBlocks.add(new BusyBlockResponse(startOfDay, date.atTime(dayConfig.getStartTime()), "CLOSED"));

                if (dayConfig.getBreakStartTime() != null && dayConfig.getBreakEndTime() != null) {
                    closedBlocks.add(new BusyBlockResponse(date.atTime(dayConfig.getBreakStartTime()), date.atTime(dayConfig.getBreakEndTime()), "BREAK"));
                }

                closedBlocks.add(new BusyBlockResponse(date.atTime(dayConfig.getEndTime()), endOfDay, "CLOSED"));
            }
        }
        return closedBlocks;
    }
}
