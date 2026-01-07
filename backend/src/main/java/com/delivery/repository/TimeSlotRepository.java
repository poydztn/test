package com.delivery.repository;

import com.delivery.entity.DeliveryMethod;
import com.delivery.entity.TimeSlot;
import com.delivery.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /**
     * Find a time slot by ID with pessimistic locking.
     * This ensures only one transaction can read and modify the slot at a time,
     * preventing double-booking issues.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdWithLock(@Param("id") Long id);

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
