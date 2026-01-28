package com.myapp.reservations.repository;

import com.myapp.reservations.entities.BusinessSchedule.TimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface TimeOffRepository extends JpaRepository<TimeOff, UUID> {

    List<TimeOff> findByScheduleSettingsBusinessId(UUID businessId);

    @Query("SELECT t FROM TimeOff t WHERE t.scheduleSettings.business.id = :businessId " +
            "AND t.startDateTime < :viewEnd " +
            "AND t.endDateTime > :viewStart")
    List<TimeOff> findByBusinessIdAndRange(
            @Param("businessId") UUID businessId,
            @Param("viewStart") LocalDateTime viewStart,
            @Param("viewEnd") LocalDateTime viewEnd
    );

    @Query("SELECT t FROM TimeOff t WHERE t.scheduleSettings.business.id = :businessId " +
            "AND :requestedDate >= t.startDateTime AND :requestedDate < t.endDateTime")
    List<TimeOff> findOverlappingTimeOff(
            @Param("businessId") UUID businessId,
            @Param("requestedDate") LocalDateTime requestedDate
    );

    @Query("SELECT COUNT(t) > 0 FROM TimeOff t WHERE t.scheduleSettings.business.id = :businessId " +
            "AND :start < t.endDateTime AND :end > t.startDateTime")
    boolean hasTimeOffConflict(
            @Param("businessId") UUID businessId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
