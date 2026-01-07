package com.delivery.service;

import com.delivery.dto.TimeSlotDTO;
import com.delivery.entity.DeliveryMethod;
import com.delivery.entity.SlotStatus;
import com.delivery.entity.TimeSlot;
import com.delivery.exception.InvalidRequestException;
import com.delivery.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing delivery time slots.
 * Generates slots on-the-fly and persists them for reservation tracking.
 */
@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    // Standard slots for DRIVE and DELIVERY methods
    private static final List<LocalTime[]> STANDARD_SLOTS = List.of(
            new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(11, 0)},
            new LocalTime[]{LocalTime.of(11, 0), LocalTime.of(13, 0)},
            new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(16, 0)},
            new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(18, 0)}
    );

    // Limited slots for DELIVERY_TODAY
    private static final List<LocalTime[]> TODAY_SLOTS = List.of(
            new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(16, 0)},
            new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(18, 0)}
    );

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Get available time slots for a delivery method and date.
     * Slots are generated on-the-fly if they don't exist.
     */
    @Transactional
    public List<TimeSlotDTO> getSlots(DeliveryMethod method, LocalDate date) {
        validateMethodAndDate(method, date);

        List<TimeSlot> slots;

        if (method == DeliveryMethod.DELIVERY_ASAP) {
            slots = generateAsapSlot(date);
        } else {
            slots = getOrCreateSlots(method, date);
        }

        return slots.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validate that the method/date combination is allowed.
     */
    public void validateMethodAndDate(DeliveryMethod method, LocalDate date) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new InvalidRequestException("Date cannot be in the past");
        }

        if (method == DeliveryMethod.DELIVERY_TODAY && !date.equals(today)) {
            throw new InvalidRequestException("DELIVERY_TODAY is only available for today's date");
        }

        if (method == DeliveryMethod.DELIVERY_ASAP && !date.equals(today)) {
            throw new InvalidRequestException("DELIVERY_ASAP is only available for today's date");
        }
    }

    /**
     * Get or create slots for standard delivery methods.
     */
    private List<TimeSlot> getOrCreateSlots(DeliveryMethod method, LocalDate date) {
        List<TimeSlot> existingSlots = timeSlotRepository.findByMethodAndDate(method, date);

        List<LocalTime[]> slotDefinitions = getSlotDefinitions(method);

        // If slots don't exist, create them
        if (existingSlots.isEmpty()) {
            List<TimeSlot> newSlots = new ArrayList<>();
            for (LocalTime[] times : slotDefinitions) {
                TimeSlot slot = new TimeSlot(method, date, times[0], times[1]);
                newSlots.add(slot);
            }
            return timeSlotRepository.saveAll(newSlots);
        }

        return existingSlots;
    }

    /**
     * Generate ASAP slot - rolling 2-hour window from now.
     */
    private List<TimeSlot> generateAsapSlot(LocalDate date) {
        LocalTime now = LocalTime.now();
        LocalTime startTime = now.withMinute(0).withSecond(0).withNano(0);
        LocalTime endTime = startTime.plusHours(2);

        // Check if we're past business hours (after 18:00)
        if (now.isAfter(LocalTime.of(18, 0))) {
            throw new InvalidRequestException("ASAP delivery is not available after 18:00");
        }

        // Cap end time at 20:00
        if (endTime.isAfter(LocalTime.of(20, 0))) {
            endTime = LocalTime.of(20, 0);
        }

        // Check if slot already exists
        TimeSlot existingSlot = timeSlotRepository
                .findByMethodAndDateAndStartTime(DeliveryMethod.DELIVERY_ASAP, date, startTime)
                .orElse(null);

        if (existingSlot != null) {
            return List.of(existingSlot);
        }

        // Create new ASAP slot
        TimeSlot asapSlot = new TimeSlot(DeliveryMethod.DELIVERY_ASAP, date, startTime, endTime);
        return List.of(timeSlotRepository.save(asapSlot));
    }

    /**
     * Get slot time definitions based on delivery method.
     */
    public List<LocalTime[]> getSlotDefinitions(DeliveryMethod method) {
        return switch (method) {
            case DRIVE, DELIVERY -> STANDARD_SLOTS;
            case DELIVERY_TODAY -> TODAY_SLOTS;
            case DELIVERY_ASAP -> List.of(); // ASAP uses dynamic generation
        };
    }

    /**
     * Convert entity to DTO.
     */
    private TimeSlotDTO toDTO(TimeSlot slot) {
        return new TimeSlotDTO(
                slot.getId(),
                slot.getMethod(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }
}
