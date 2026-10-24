package com.airfrance.airfrance.web;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String status,
        String language,
        int revision) {
}
