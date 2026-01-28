package com.myapp.reservations.entities.BusinessSchedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkingDay {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    private boolean isDayOff;

    @ManyToOne
    @JoinColumn(name = "schedule_settings_id")
    private ScheduleSettings scheduleSettings;
}
