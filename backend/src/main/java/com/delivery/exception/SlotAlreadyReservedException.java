package com.delivery.exception;

/**
 * Exception thrown when attempting to reserve an already reserved slot.
 */
public class SlotAlreadyReservedException extends RuntimeException {
    
    public SlotAlreadyReservedException(String message) {
        super(message);
    }

    public SlotAlreadyReservedException(Long slotId) {
        super("Time slot " + slotId + " is already reserved");
    }
}
