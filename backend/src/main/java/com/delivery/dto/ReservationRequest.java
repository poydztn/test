package com.delivery.dto;

import com.delivery.entity.DeliveryMethod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request DTO for creating a reservation.
 */
public record ReservationRequest(
        @NotNull(message = "Delivery method is required")
        DeliveryMethod method,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Slot ID is required")
        Long slotId
) {}
