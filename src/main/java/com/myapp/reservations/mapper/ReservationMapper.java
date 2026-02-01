package com.myapp.reservations.mapper;

import com.myapp.reservations.dto.reservationdto.ReservationRequest;
import com.myapp.reservations.dto.reservationdto.ReservationResponse;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.reservation.Reservation;
import com.myapp.reservations.entities.reservation.ReservationStatus;
import com.myapp.reservations.entities.businessSchedule.Offering;
import com.myapp.reservations.entities.user.User;

import java.time.LocalDateTime;

public class ReservationMapper {

    public static Reservation toReservation(ReservationRequest request, Business business, Offering offering, User user) {
        if (request == null) return null;

        Reservation reservation = new Reservation();
        reservation.setBusiness(business);
        reservation.setOffering(offering);
        reservation.setUser(user);

        reservation.setStartDateTime(request.startTime());

        if (offering != null && offering.getDurationMinutes() != null) {
            reservation.setEndDateTime(request.startTime().plusMinutes(offering.getDurationMinutes()));
        }

        reservation.setStatus(ReservationStatus.PENDING);
        return reservation;
    }

    public static ReservationResponse toResponse(Reservation reservation) {
        if (reservation == null) return null;

        return new ReservationResponse(
                reservation.getId(),
                reservation.getBusiness().getId(),
                reservation.getBusiness().getName(),
                reservation.getOffering().getId(),
                reservation.getOffering().getName(),
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getStatus(),
                LocalDateTime.now()
        );
    }

}