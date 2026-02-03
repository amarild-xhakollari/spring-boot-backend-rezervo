package com.myapp.reservations.service;

import com.myapp.reservations.dto.reservationdto.ReservationRequest;
import com.myapp.reservations.dto.reservationdto.ReservationResponse;
import com.myapp.reservations.exception.UnauthorizedException;
import com.myapp.reservations.exception.businessruleviolations.*;
import com.myapp.reservations.exception.conflictexceptions.ReservationConflictException;
import com.myapp.reservations.exception.notfoundexceptions.BusinessNotFoundException;
import com.myapp.reservations.exception.notfoundexceptions.OfferingNotFoundException;
import com.myapp.reservations.exception.notfoundexceptions.ReservationNotFoundException;
import com.myapp.reservations.exception.notfoundexceptions.ScheduleNotFoundException;
import com.myapp.reservations.exception.notfoundexceptions.UserNotFoundException;
import com.myapp.reservations.mapper.ReservationMapper;
import com.myapp.reservations.repository.*;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.businessSchedule.*;
import com.myapp.reservations.entities.notification.NotificationType;
import com.myapp.reservations.entities.reservation.Reservation;
import com.myapp.reservations.entities.reservation.ReservationStatus;
import com.myapp.reservations.entities.user.User;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ReservationService {

    private final BusinessRepository businessRepository;
    private final ReservationRepository reservationRepository;
    private final ScheduleSettingsRepository scheduleSettingsRepository;
    private final OfferingRepository offeringRepository;
    private final TimeOffRepository timeOffRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    public ReservationService(BusinessRepository businessRepository, ReservationRepository reservationRepository,
                              ScheduleSettingsRepository scheduleSettingsRepository, OfferingRepository offeringRepository,
                              UserRepository userRepository, TimeOffRepository timeOffRepository,
                              UserService userService, @Lazy NotificationService notificationService) {
        this.businessRepository = businessRepository;
        this.reservationRepository = reservationRepository;
        this.scheduleSettingsRepository = scheduleSettingsRepository;
        this.offeringRepository = offeringRepository;
        this.timeOffRepository = timeOffRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    private void validateWorkingHours(LocalDateTime startDateTime, LocalDateTime endDateTime, ScheduleSettings settings) {
        DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();

        WorkingDay config = settings.getWorkingDays().stream()
                .filter(wd -> wd.getDayOfWeek().equals(dayOfWeek))
                .findFirst()
                .orElseThrow(() -> new BusinessClosedException(dayOfWeek));

        if (config.isDayOff()) {
            throw new BusinessClosedException(dayOfWeek);
        }

        LocalTime requestStart = startDateTime.toLocalTime();
        LocalTime requestEnd = endDateTime.toLocalTime();

        if (requestStart.isBefore(config.getStartTime()) || requestEnd.isAfter(config.getEndTime())) {
            throw new OutsideWorkingHoursException(requestStart, config.getStartTime(), config.getEndTime());
        }

        if (config.getBreakStartTime() != null && requestStart.isBefore(config.getBreakEndTime()) && requestEnd.isAfter(config.getBreakStartTime())) {
            throw new BreakTimeConflictException(config.getBreakStartTime(), config.getBreakEndTime());
        }
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest reservationRequest){

        if(reservationRequest == null){
            throw new IllegalArgumentException("Reservation request cannot be null");
        }

        Offering offering = offeringRepository.findById(reservationRequest.offeringId())
                .orElseThrow(() -> new OfferingNotFoundException(reservationRequest.offeringId()));

        LocalDateTime startDateTime = reservationRequest.startTime();
        LocalDateTime endDateTime = startDateTime.plusMinutes(offering.getDurationMinutes());

        com.myapp.reservations.entities.businessSchedule.ScheduleSettings schedule = scheduleSettingsRepository.getScheduleSettingsByBusinessId(reservationRequest.businessId())
                .orElseThrow(() -> new ScheduleNotFoundException(reservationRequest.businessId()));

        validateWorkingHours(startDateTime, endDateTime, schedule);

        validateAdvanceBookingRequirements(startDateTime, schedule);

        Business business = businessRepository.getBusinessById(reservationRequest.businessId())
                .orElseThrow(()-> new BusinessNotFoundException(reservationRequest.businessId()));

        if (reservationRepository.existsOverlap(business.getId(), startDateTime, endDateTime)) {
            throw new ReservationConflictException(startDateTime);
        }

        if (timeOffRepository.hasTimeOffConflict(business.getId(), startDateTime, endDateTime)) {
            throw new BusinessUnavailableException();
        }

        Reservation reservation =  new Reservation();
        reservation.setBusiness(business);
        reservation.setOffering(offering);

        UUID currentUserId = userService.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        reservation.setUser(user);

        reservation.setStartDateTime(startDateTime);
        reservation.setEndDateTime(endDateTime);

        if (schedule.getAutoConfirmAppointments() == true) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
        } else {
            reservation.setStatus(ReservationStatus.PENDING);
        }
        reservation.setCreatedAt(LocalDateTime.now());

        reservationRepository.save(reservation);

        String formattedDate = startDateTime.format(DATE_FORMATTER);
        String notificationTitle = schedule.getAutoConfirmAppointments()
                ? "New Reservation Confirmed"
                : "New Reservation Request";
        String notificationMessage = String.format(
                "%s booked '%s' for %s.%s",
                user.getName(),
                offering.getName(),
                formattedDate,
                schedule.getAutoConfirmAppointments() ? "" : " Please review and confirm."
        );
        NotificationType notificationType = schedule.getAutoConfirmAppointments()
                ? NotificationType.SUCCESS
                : NotificationType.INFO;

        notificationService.createNotificationForUser(
                business.getOwner().getId(),
                notificationTitle,
                notificationMessage,
                notificationType,
                "/dashboard"
        );

        if (schedule.getAutoConfirmAppointments()) {
            notificationService.createNotificationForUser(
                    user.getId(),
                    "Reservation Confirmed",
                    String.format("Your reservation at %s for '%s' on %s has been confirmed.",
                            business.getName(), offering.getName(), formattedDate),
                    NotificationType.SUCCESS,
                    "/reservations"
            );
        } else {
            notificationService.createNotificationForUser(
                    user.getId(),
                    "Reservation Received",
                    String.format("Your reservation request at %s for '%s' on %s has been received. The business will review and confirm shortly.",
                            business.getName(), offering.getName(), formattedDate),
                    NotificationType.INFO,
                    "/reservations"
            );
        }

        return ReservationMapper.toResponse(reservation);
    }

    private void validateAdvanceBookingRequirements(LocalDateTime requestedStart, ScheduleSettings settings) {
        LocalDateTime now = LocalDateTime.now();

        if (settings.getMinAdvanceBookingHours() != null) {
            LocalDateTime earliestAllowed = now.plusHours(settings.getMinAdvanceBookingHours());
            if (requestedStart.isBefore(earliestAllowed)) {
                throw new BookingLeadTimeException(settings.getMinAdvanceBookingHours());
            }
        }

        if (settings.getMaxAdvanceBookingDays() != null) {
            LocalDateTime latestAllowed = now.plusDays(settings.getMaxAdvanceBookingDays());
            if (requestedStart.isAfter(latestAllowed)) {
                throw new BookingWindowException(settings.getMaxAdvanceBookingDays());
            }
        }

        if (requestedStart.isBefore(now)) {
            throw new PastDateReservationException(requestedStart);
        }
    }

    @Transactional
    public void cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidReservationStateException("CANCELLED", "cancel");
        }

        UUID currentUserId = userService.getCurrentUserId();
        boolean isCustomer = reservation.getUser().getId().equals(currentUserId);
        boolean isBusinessOwner = reservation.getBusiness().getOwner().getId().equals(currentUserId);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        String formattedDate = reservation.getStartDateTime().format(DATE_FORMATTER);

        if (isCustomer) {
            notificationService.createNotificationForUser(
                    reservation.getBusiness().getOwner().getId(),
                    "Reservation Cancelled",
                    String.format("%s cancelled their reservation for '%s' on %s.",
                            reservation.getUser().getName(),
                            reservation.getOffering().getName(),
                            formattedDate),
                    NotificationType.WARNING,
                    "/dashboard"
            );
        } else if (isBusinessOwner) {
            notificationService.createNotificationForUser(
                    reservation.getUser().getId(),
                    "Reservation Cancelled",
                    String.format("Your reservation at %s for '%s' on %s has been cancelled by the business.",
                            reservation.getBusiness().getName(),
                            reservation.getOffering().getName(),
                            formattedDate),
                    NotificationType.ALERT,
                    "/reservations"
            );
        }
    }

    @Transactional
    public ReservationResponse confirmReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        UUID currentUserId = userService.getCurrentUserId();
        if (!reservation.getBusiness().getOwner().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the business owner can confirm reservations");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidReservationStateException(reservation.getStatus().toString(), "confirm");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        String formattedDate = reservation.getStartDateTime().format(DATE_FORMATTER);

        notificationService.createNotificationForUser(
                reservation.getUser().getId(),
                "Reservation Confirmed",
                String.format("Your reservation at %s for '%s' on %s has been confirmed!",
                        reservation.getBusiness().getName(),
                        reservation.getOffering().getName(),
                        formattedDate),
                NotificationType.SUCCESS,
                "/reservations"
        );

        return ReservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse rejectReservation(UUID reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        UUID currentUserId = userService.getCurrentUserId();
        if (!reservation.getBusiness().getOwner().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the business owner can reject reservations");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidReservationStateException(reservation.getStatus().toString(), "reject");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        String formattedDate = reservation.getStartDateTime().format(DATE_FORMATTER);
        String reasonText = (reason != null && !reason.isBlank()) ? " Reason: " + reason : "";

        notificationService.createNotificationForUser(
                reservation.getUser().getId(),
                "Reservation Rejected",
                String.format("Your reservation at %s for '%s' on %s was not approved.%s",
                        reservation.getBusiness().getName(),
                        reservation.getOffering().getName(),
                        formattedDate,
                        reasonText),
                NotificationType.ALERT,
                "/reservations"
        );

        return ReservationMapper.toResponse(reservation);
    }

    public java.util.List<ReservationResponse> getMyReservations(UUID userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(ReservationMapper::toResponse)
                .toList();
    }

    public java.util.List<ReservationResponse> getReservationsByBusiness(UUID businessId) {
        return reservationRepository.findByBusinessId(businessId).stream()
                .map(ReservationMapper::toResponse)
                .toList();
    }

}
