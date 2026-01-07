package com.delivery.dto;

import com.delivery.entity.DeliveryMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request DTO for creating a reservation.
 */
public class ReservationRequest {

    @NotNull(message = "Delivery method is required")
    private DeliveryMethod method;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Slot ID is required")
    private Long slotId;



    // Getters and Setters
    public DeliveryMethod getMethod() { return method; }
    public void setMethod(DeliveryMethod method) { this.method = method; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

}
