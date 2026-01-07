package com.delivery.controller;

import com.delivery.dto.TimeSlotDTO;
import com.delivery.entity.DeliveryMethod;
import com.delivery.exception.InvalidRequestException;
import com.delivery.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for time slots.
 */
@RestController
@RequestMapping("/api/time-slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    /**
     * Get available time slots for a delivery method and date.
     *
     * @param method Delivery method (DRIVE, DELIVERY, DELIVERY_TODAY, DELIVERY_ASAP)
     * @param date   Date in YYYY-MM-DD format
     * @return List of time slots with availability status
     */
    @GetMapping
    public List<TimeSlotDTO> getTimeSlots(
            @RequestParam("method") String method,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        DeliveryMethod deliveryMethod;
        try {
            deliveryMethod = DeliveryMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid delivery method: " + method);
        }

        return timeSlotService.getSlots(deliveryMethod, date);
    }
}
