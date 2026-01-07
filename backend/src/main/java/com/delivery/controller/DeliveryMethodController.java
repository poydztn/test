package com.delivery.controller;

import com.delivery.entity.DeliveryMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * REST controller for delivery methods.
 */
@RestController
@RequestMapping("/api/delivery-methods")
public class DeliveryMethodController {

    /**
     * Get all available delivery methods as simple strings.
     */
    @GetMapping
    public List<String> getDeliveryMethods() {
        return Arrays.stream(DeliveryMethod.values())
                .map(Enum::name)
                .toList();
    }
}
