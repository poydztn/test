package com.delivery.dto;

import com.delivery.entity.DeliveryMethod;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for reservation response.
 */
public record ReservationDTO(
        Long id,
        Long slotId,
        DeliveryMethod method,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
