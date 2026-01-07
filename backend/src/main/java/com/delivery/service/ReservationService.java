package com.delivery.service;

import com.delivery.dto.ReservationDTO;
import com.delivery.dto.ReservationRequest;
import com.delivery.entity.Reservation;
import com.delivery.entity.SlotStatus;
import com.delivery.entity.TimeSlot;
import com.delivery.exception.InvalidRequestException;
import com.delivery.exception.SlotAlreadyReservedException;
import com.delivery.repository.ReservationRepository;
import com.delivery.repository.TimeSlotRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

        // Find the time slot with pessimistic lock to prevent concurrent reservations
        TimeSlot slot = timeSlotRepository.findByIdWithLock(request.getSlotId())
                .orElseThrow(() -> new InvalidRequestException("Time slot not found: " + request.getSlotId()));

        // Verify slot matches request
        if (slot.getMethod() != request.getMethod() || !slot.getDate().equals(request.getDate())) {
            throw new InvalidRequestException("Slot does not match specified method and date");
        }

        // Check if slot is already reserved
        if (slot.getStatus() == SlotStatus.RESERVED) {
            throw new SlotAlreadyReservedException(slot.getId());
        }

        try {
            // Mark slot as reserved (optimistic lock will catch concurrent updates)
            slot.setStatus(SlotStatus.RESERVED);
            timeSlotRepository.save(slot);

            // Create reservation
            Reservation reservation = new Reservation(slot, request.getCustomerId());
            reservation = reservationRepository.save(reservation);

            return toDTO(reservation);

        } catch (ObjectOptimisticLockingFailureException e) {
            // Concurrent update detected - slot was reserved by another user
            throw new SlotAlreadyReservedException(slot.getId());
        }
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
                slot.getEndTime(),
                reservation.getCustomerId(),
                reservation.getCreatedAt()
        );
    }
}
