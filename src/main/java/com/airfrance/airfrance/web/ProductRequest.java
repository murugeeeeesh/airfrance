package com.airfrance.airfrance.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 100) String sku,
        @NotBlank @Size(max = 200) String nameEn,
        @NotBlank @Size(max = 200) String nameFr,
        @NotBlank @Size(max = 4000) String descriptionEn,
        @NotBlank @Size(max = 4000) String descriptionFr,
        @NotBlank @Size(max = 120) String categoryEn,
        @NotBlank @Size(max = 120) String categoryFr,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") String status) {
}
