package com.delivery.repository;

import com.delivery.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find all reservations by customer ID.
     */
    List<Reservation> findByCustomerId(String customerId);

    /**
     * Check if a reservation exists for a given time slot.
     */
    boolean existsByTimeSlotId(Long timeSlotId);
}
