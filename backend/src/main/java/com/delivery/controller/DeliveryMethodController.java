package com.delivery.controller;

import com.delivery.entity.DeliveryMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for delivery methods.
 */
@RestController
@RequestMapping("/api/delivery-methods")
public class DeliveryMethodController {

    /**
     * Get all available delivery methods with descriptions.
     */
    @GetMapping
    public List<Map<String, String>> getDeliveryMethods() {
        return Arrays.stream(DeliveryMethod.values())
                .map(method -> Map.of(
                        "code", method.name(),
                        "name", formatMethodName(method),
                        "description", getMethodDescription(method)
                ))
                .collect(Collectors.toList());
    }

    private String formatMethodName(DeliveryMethod method) {
        return method.name();
    }

    private String getMethodDescription(DeliveryMethod method) {
        return switch (method) {
            case DRIVE -> "Pick up your order at a store location. Available any day.";
            case DELIVERY -> "Standard home delivery. Available any day.";
            case DELIVERY_TODAY -> "Delivery today. Limited slots available.";
            case DELIVERY_ASAP -> "Express delivery within 2 hours. Today only.";
        };
    }
}
