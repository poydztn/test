package com.delivery.service;

import com.delivery.dto.ReservationDTO;
import com.delivery.dto.ReservationRequest;
import com.delivery.entity.DeliveryMethod;
import com.delivery.entity.Reservation;
import com.delivery.entity.SlotStatus;
import com.delivery.entity.TimeSlot;
import com.delivery.exception.InvalidRequestException;
import com.delivery.exception.SlotAlreadyReservedException;
import com.delivery.repository.ReservationRepository;
import com.delivery.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationService with Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TimeSlotService timeSlotService;

    @InjectMocks
    private ReservationService reservationService;

    private LocalDate today;
    private TimeSlot availableSlot;
    private TimeSlot reservedSlot;
    private ReservationRequest validRequest;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        
        availableSlot = new TimeSlot(DeliveryMethod.DRIVE, today, LocalTime.of(9, 0), LocalTime.of(11, 0));
        availableSlot.setId(1L);
        availableSlot.setStatus(SlotStatus.AVAILABLE);

        reservedSlot = new TimeSlot(DeliveryMethod.DRIVE, today, LocalTime.of(11, 0), LocalTime.of(13, 0));
        reservedSlot.setId(2L);
        reservedSlot.setStatus(SlotStatus.RESERVED);

        validRequest = new ReservationRequest();
        validRequest.setMethod(DeliveryMethod.DRIVE);
        validRequest.setDate(today);
        validRequest.setSlotId(1L);
    }

    @Test
    @DisplayName("Should create reservation successfully")
    void createReservation_ValidRequest_ReturnsReservation() {
        // Arrange
        doNothing().when(timeSlotService).validateMethodAndDate(any(), any());
        when(timeSlotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(availableSlot));
        when(timeSlotRepository.save(any())).thenReturn(availableSlot);
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        // Act
        ReservationDTO result = reservationService.createReservation(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getSlotId());
        assertEquals(DeliveryMethod.DRIVE, result.getMethod());
        
        verify(timeSlotRepository).save(any());
        verify(reservationRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when slot is already reserved")
    void createReservation_AlreadyReserved_ThrowsException() {
        // Arrange
        validRequest.setSlotId(2L);
        doNothing().when(timeSlotService).validateMethodAndDate(any(), any());
        when(timeSlotRepository.findByIdWithLock(2L)).thenReturn(Optional.of(reservedSlot));

        // Act & Assert
        assertThrows(
                SlotAlreadyReservedException.class,
                () -> reservationService.createReservation(validRequest)
        );
        
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when slot not found")
    void createReservation_SlotNotFound_ThrowsException() {
        // Arrange
        doNothing().when(timeSlotService).validateMethodAndDate(any(), any());
        when(timeSlotRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> reservationService.createReservation(validRequest)
        );
        assertTrue(exception.getMessage().contains("Time slot not found"));
    }

    @Test
    @DisplayName("Should throw exception on concurrent reservation (optimistic lock)")
    void createReservation_ConcurrentUpdate_ThrowsException() {
        // Arrange
        doNothing().when(timeSlotService).validateMethodAndDate(any(), any());
        when(timeSlotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(availableSlot));
        when(timeSlotRepository.save(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(TimeSlot.class, 1L));

        // Act & Assert
        assertThrows(
                SlotAlreadyReservedException.class,
                () -> reservationService.createReservation(validRequest)
        );
    }

    @Test
    @DisplayName("Should throw exception when method does not match slot")
    void createReservation_MethodMismatch_ThrowsException() {
        // Arrange
        validRequest.setMethod(DeliveryMethod.DELIVERY); // Different from slot's DRIVE
        doNothing().when(timeSlotService).validateMethodAndDate(any(), any());
        when(timeSlotRepository.findByIdWithLock(1L)).thenReturn(Optional.of(availableSlot));

        // Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> reservationService.createReservation(validRequest)
        );
        assertTrue(exception.getMessage().contains("does not match"));
    }

    @Test
    @DisplayName("Should get reservation by ID")
    void getReservation_ValidId_ReturnsReservation() {
        // Arrange
        Reservation reservation = new Reservation(availableSlot);
        reservation.setId(100L);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        // Act
        ReservationDTO result = reservationService.getReservation(100L);

        // Assert
        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when reservation not found")
    void getReservation_NotFound_ThrowsException() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidRequestException.class,
                () -> reservationService.getReservation(999L)
        );
    }
}
