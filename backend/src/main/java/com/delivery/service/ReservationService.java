package com.delivery.service;

import com.delivery.dto.ReservationDTO;
import com.delivery.dto.ReservationRequest;
import com.delivery.entity.Reservation;
import com.delivery.entity.TimeSlot;
import com.delivery.exception.InvalidRequestException;
import com.delivery.repository.ReservationRepository;
import com.delivery.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing reservations.
 * Handles concurrency via optimistic locking.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotService timeSlotService;

    public ReservationService(ReservationRepository reservationRepository,
                              TimeSlotRepository timeSlotRepository,
                              TimeSlotService timeSlotService) {
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotService = timeSlotService;
    }

    /**
     * Create a reservation for a time slot.
     * Uses optimistic locking to prevent double-booking.
     */
    @Transactional
    public ReservationDTO createReservation(ReservationRequest request) {
        // Validate method/date combination
        timeSlotService.validateMethodAndDate(request.getMethod(), request.getDate());

        // Find the time slot
        TimeSlot slot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new InvalidRequestException("Time slot not found: " + request.getSlotId()));

        // Verify slot matches request
        if (slot.getMethod() != request.getMethod() || !slot.getDate().equals(request.getDate())) {
            throw new InvalidRequestException("Slot does not match specified method and date");
        }

        // Create reservation
        Reservation reservation = new Reservation(slot);
        reservation = reservationRepository.save(reservation);

        return toDTO(reservation);
    }

    /**
     * Get reservation by ID.
     */
    public ReservationDTO getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Reservation not found: " + id));
        return toDTO(reservation);
    }

    /**
     * Convert entity to DTO.
     */
    private ReservationDTO toDTO(Reservation reservation) {
        TimeSlot slot = reservation.getTimeSlot();
        return new ReservationDTO(
                reservation.getId(),
                slot.getId(),
                slot.getMethod(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime()
        );
    }
}
