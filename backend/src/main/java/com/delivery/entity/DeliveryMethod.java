package com.delivery.entity;

/**
 * Delivery method types available for scheduling.
 */
public enum DeliveryMethod {
    DRIVE,           // Store pickup - any future date
    DELIVERY,        // Standard delivery - any future date
    DELIVERY_TODAY,  // Same-day delivery - today only
    DELIVERY_ASAP    // Express delivery - today only, 2-hour window
}
