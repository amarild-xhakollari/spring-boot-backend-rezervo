package com.myapp.reservations.repository;

import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleSettingsRepository extends JpaRepository<ScheduleSettings, UUID> {

    ScheduleSettings getScheduleSettingsById(UUID scheduleId);
    Optional<ScheduleSettings> getScheduleSettingsByBusinessId(UUID businessId);
    List<ScheduleSettings> findAll();
 }
