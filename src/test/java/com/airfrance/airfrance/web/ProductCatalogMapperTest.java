package com.airfrance.airfrance.web;

import com.airfrance.airfrance.entity.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCatalogMapperTest {

    private final ProductCatalogMapper mapper = new ProductCatalogMapper();

    @Test
    void mapsBothTranslationsIntoVersionTwoResponse() {
        Product product = new Product("SKU-1", "Book", "Livre", "A book", "Un livre",
                "Books", "Livres", new BigDecimal("12.50"), "EUR", "ACTIVE");

        ProductV2Response response = mapper.toV2Response(product);

        assertEquals("Book", response.translations().get("en").name());
        assertEquals("Un livre", response.translations().get("fr").description());
        assertEquals("Livres", response.translations().get("fr").category());
    }

    @Test
    void mapsLocalizedResponseWithRequestedLanguage() {
        Product product = new Product("SKU-1", "Book", "Livre", "A book", "Un livre",
                "Books", "Livres", new BigDecimal("12.50"), "EUR", "ACTIVE");

        ProductResponse response = mapper.toLocalizedResponse(product, "fr");

        assertEquals("Livre", response.name());
        assertEquals("fr", response.language());
        assertEquals(1, response.revision());
    }
}
