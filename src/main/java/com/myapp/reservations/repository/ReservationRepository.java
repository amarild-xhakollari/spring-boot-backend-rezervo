package com.myapp.reservations.repository;

import com.myapp.reservations.entities.Reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("SELECT r FROM Reservation r WHERE r.business.id = :businessId " +
            "AND r.status != 'CANCELLED' " +
            "AND r.endDateTime > :viewStart AND r.startDateTime < :viewEnd")
    List<Reservation> findActiveByBusinessInRange(UUID businessId, LocalDateTime viewStart, LocalDateTime viewEnd);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.business.id = :businessId " +
            "AND r.status != 'CANCELLED' " +
            "AND :start < r.endDateTime AND :end > r.startDateTime")
    boolean existsOverlap(UUID businessId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByUserId(UUID userId);

    List<Reservation> findByBusinessId(UUID businessId);

}
