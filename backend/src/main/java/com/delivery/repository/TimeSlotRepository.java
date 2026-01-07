package com.delivery.repository;

import com.delivery.entity.DeliveryMethod;
import com.delivery.entity.TimeSlot;
import com.delivery.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /**
     * Find all slots for a given method and date.
     */
    List<TimeSlot> findByMethodAndDate(DeliveryMethod method, LocalDate date);

    /**
     * Find a specific slot by method, date, and start time.
     */
    Optional<TimeSlot> findByMethodAndDateAndStartTime(
            DeliveryMethod method, LocalDate date, LocalTime startTime);

    /**
     * Check if a slot exists for method, date, and time (for duplicate prevention).
     */
    boolean existsByMethodAndDateAndStartTime(
            DeliveryMethod method, LocalDate date, LocalTime startTime);
}
