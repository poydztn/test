package com.delivery.dto;

import com.delivery.entity.DeliveryMethod;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for reservation response.
 */
public class ReservationDTO {
    private Long id;
    private Long slotId;
    private DeliveryMethod method;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public ReservationDTO() {}

    public ReservationDTO(Long id, Long slotId, DeliveryMethod method, LocalDate date,
                          LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.slotId = slotId;
        this.method = method;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public DeliveryMethod getMethod() { return method; }
    public void setMethod(DeliveryMethod method) { this.method = method; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
