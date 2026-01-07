package com.delivery.service;

import com.delivery.dto.TimeSlotDTO;
import com.delivery.entity.DeliveryMethod;

import com.delivery.entity.TimeSlot;
import com.delivery.exception.InvalidRequestException;
import com.delivery.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimeSlotService with Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private LocalDate today;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
    }

    @Test
    @DisplayName("Should return 4 standard slots for DRIVE method")
    void getSlots_DriveMethod_ReturnsFourSlots() {
        // Arrange
        when(timeSlotRepository.findByMethodAndDate(DeliveryMethod.DRIVE, tomorrow))
                .thenReturn(Collections.emptyList());
        when(timeSlotRepository.saveAll(any()))
                .thenAnswer(invocation -> {
                    List<TimeSlot> slots = invocation.getArgument(0);
                    for (int i = 0; i < slots.size(); i++) {
                        slots.get(i).setId((long) (i + 1));
                    }
                    return slots;
                });

        // Act
        List<TimeSlotDTO> slots = timeSlotService.getSlots(DeliveryMethod.DRIVE, tomorrow);

        // Assert
        assertEquals(4, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStartTime());
        assertEquals(LocalTime.of(11, 0), slots.get(0).getEndTime());
        assertEquals(LocalTime.of(16, 0), slots.get(3).getStartTime());
        assertEquals(LocalTime.of(18, 0), slots.get(3).getEndTime());

        verify(timeSlotRepository).saveAll(any());
    }

    @Test
    @DisplayName("Should return 4 standard slots for DELIVERY method")
    void getSlots_DeliveryMethod_ReturnsFourSlots() {
        // Arrange
        when(timeSlotRepository.findByMethodAndDate(DeliveryMethod.DELIVERY, tomorrow))
                .thenReturn(Collections.emptyList());
        when(timeSlotRepository.saveAll(any()))
                .thenAnswer(invocation -> {
                    List<TimeSlot> slots = invocation.getArgument(0);
                    for (int i = 0; i < slots.size(); i++) {
                        slots.get(i).setId((long) (i + 1));
                    }
                    return slots;
                });

        // Act
        List<TimeSlotDTO> slots = timeSlotService.getSlots(DeliveryMethod.DELIVERY, tomorrow);

        // Assert
        assertEquals(4, slots.size());

    }

    @Test
    @DisplayName("Should return 2 limited slots for DELIVERY_TODAY method")
    void getSlots_DeliveryTodayMethod_ReturnsTwoSlots() {
        // Arrange
        when(timeSlotRepository.findByMethodAndDate(DeliveryMethod.DELIVERY_TODAY, today))
                .thenReturn(Collections.emptyList());
        when(timeSlotRepository.saveAll(any()))
                .thenAnswer(invocation -> {
                    List<TimeSlot> slots = invocation.getArgument(0);
                    for (int i = 0; i < slots.size(); i++) {
                        slots.get(i).setId((long) (i + 1));
                    }
                    return slots;
                });

        // Act
        List<TimeSlotDTO> slots = timeSlotService.getSlots(DeliveryMethod.DELIVERY_TODAY, today);

        // Assert
        assertEquals(2, slots.size());
        assertEquals(LocalTime.of(14, 0), slots.get(0).getStartTime());
        assertEquals(LocalTime.of(16, 0), slots.get(1).getStartTime());
    }

    @Test
    @DisplayName("Should throw exception when DELIVERY_TODAY is requested for future date")
    void getSlots_DeliveryTodayFutureDate_ThrowsException() {
        // Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> timeSlotService.getSlots(DeliveryMethod.DELIVERY_TODAY, tomorrow)
        );
        assertEquals("DELIVERY_TODAY is only available for today's date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when DELIVERY_ASAP is requested for future date")
    void getSlots_DeliveryAsapFutureDate_ThrowsException() {
        // Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> timeSlotService.getSlots(DeliveryMethod.DELIVERY_ASAP, tomorrow)
        );
        assertEquals("DELIVERY_ASAP is only available for today's date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when date is in the past")
    void getSlots_PastDate_ThrowsException() {
        // Arrange
        LocalDate yesterday = today.minusDays(1);

        // Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> timeSlotService.getSlots(DeliveryMethod.DRIVE, yesterday)
        );
        assertEquals("Date cannot be in the past", exception.getMessage());
    }

    @Test
    @DisplayName("Should return existing slots without creating new ones")
    void getSlots_ExistingSlots_ReturnsWithoutCreating() {
        // Arrange
        List<TimeSlot> existingSlots = List.of(
                createSlot(1L, DeliveryMethod.DRIVE, tomorrow, LocalTime.of(9, 0), LocalTime.of(11, 0)),
                createSlot(2L, DeliveryMethod.DRIVE, tomorrow, LocalTime.of(11, 0), LocalTime.of(13, 0))
        );
        when(timeSlotRepository.findByMethodAndDate(DeliveryMethod.DRIVE, tomorrow))
                .thenReturn(existingSlots);

        // Act
        List<TimeSlotDTO> slots = timeSlotService.getSlots(DeliveryMethod.DRIVE, tomorrow);

        // Assert
        assertEquals(2, slots.size());
        verify(timeSlotRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should return slot definitions for different methods")
    void getSlotDefinitions_ReturnsCorrectSlots() {
        // Assert
        assertEquals(4, timeSlotService.getSlotDefinitions(DeliveryMethod.DRIVE).size());
        assertEquals(4, timeSlotService.getSlotDefinitions(DeliveryMethod.DELIVERY).size());
        assertEquals(2, timeSlotService.getSlotDefinitions(DeliveryMethod.DELIVERY_TODAY).size());
        assertEquals(0, timeSlotService.getSlotDefinitions(DeliveryMethod.DELIVERY_ASAP).size());
    }

    private TimeSlot createSlot(Long id, DeliveryMethod method, LocalDate date, 
                                 LocalTime start, LocalTime end) {
        TimeSlot slot = new TimeSlot(method, date, start, end);
        slot.setId(id);
        return slot;
    }
}
