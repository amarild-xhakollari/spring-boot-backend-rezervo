package com.myapp.reservations.repository;

import com.myapp.reservations.entities.BusinessSchedule.Offering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {
    Offering getOfferingById(UUID offeringId);
    void deleteById(UUID offeringId);
}
