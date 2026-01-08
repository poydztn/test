package com.delivery.dto;

import com.delivery.entity.DeliveryMethod;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for time slot response.
 */
public record TimeSlotDTO(
        Long id,
        DeliveryMethod method,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
