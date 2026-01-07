package com.delivery;

import com.delivery.dto.ReservationRequest;
import com.delivery.entity.DeliveryMethod;
import com.delivery.entity.SlotStatus;
import com.delivery.entity.TimeSlot;
import com.delivery.repository.TimeSlotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for concurrency handling.
 * Tests that only one reservation succeeds when multiple users
 * try to reserve the same slot simultaneously.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReservationConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Only one reservation succeeds when concurrent requests target same slot")
    void concurrentReservations_OnlyOneSucceeds() throws Exception {
        // Arrange - Create a fresh slot for this test
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        TimeSlot slot = new TimeSlot(DeliveryMethod.DRIVE, tomorrow, 
                LocalTime.of(10, 0), LocalTime.of(12, 0));
        slot = timeSlotRepository.save(slot);
        final Long slotId = slot.getId();

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // Act - Launch concurrent reservation attempts
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    ReservationRequest request = new ReservationRequest();
                    request.setMethod(DeliveryMethod.DRIVE);
                    request.setDate(tomorrow);
                    request.setSlotId(slotId);

                    MvcResult result = mockMvc.perform(post("/api/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andReturn();

                    int statusCode = result.getResponse().getStatus();
                    if (statusCode == 201) {
                        successCount.incrementAndGet();
                    } else if (statusCode == 409) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Handle exception
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // Assert - Exactly one should succeed
        assertEquals(1, successCount.get(), 
                "Exactly one reservation should succeed");
        assertEquals(numberOfThreads - 1, conflictCount.get(), 
                "All other reservations should receive 409 Conflict");

        // Verify slot is now reserved
        TimeSlot updatedSlot = timeSlotRepository.findById(slotId).orElseThrow();
        assertEquals(SlotStatus.RESERVED, updatedSlot.getStatus(), 
                "Slot should be marked as RESERVED");
    }
}
