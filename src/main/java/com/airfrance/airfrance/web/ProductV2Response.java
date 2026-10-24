package com.airfrance.airfrance.web;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ProductV2Response(
        UUID id,
        String sku,
        BigDecimal price,
        String currency,
        String status,
        int revision,
        Map<String, ProductTranslation> translations) {
}
