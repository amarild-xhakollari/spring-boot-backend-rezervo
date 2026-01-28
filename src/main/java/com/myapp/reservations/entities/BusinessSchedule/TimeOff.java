package com.myapp.reservations.entities.BusinessSchedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_off")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "schedule_settings_id")
    private ScheduleSettings scheduleSettings;
}
