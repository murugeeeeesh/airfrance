package com.airfrance.airfrance.web;

import java.time.Instant;
import java.math.BigDecimal;

public record ProductRevisionResponse(
        int revision,
        Instant changedAt,
        String changedBy,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        String currency,
        String status,
        String language) {
}
