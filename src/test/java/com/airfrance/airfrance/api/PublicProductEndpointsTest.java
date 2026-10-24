package com.airfrance.airfrance.api;

import com.airfrance.airfrance.entity.Product;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicProductEndpointsTest extends AbstractProductApiIntegrationTest {

    @Test
    void versionOneListingAndDetailsReturnRequestedLanguage() throws Exception {
        Product saved = saveProduct("PUBLIC-" + java.util.UUID.randomUUID(), "ACTIVE");
        String id = saved.getId().toString();

        var list = request("GET", "/api/v1/products?search=Kit%20de%20voyage&page=0&size=10",
                null, null, "fr-CA,fr;q=0.9,en;q=0.8");
        JsonNode listBody = json(list.body());
        assertEquals(200, list.statusCode());
        assertEquals(1, listBody.get("totalElements").asInt());
        assertEquals("Kit de voyage", listBody.get("content").get(0).get("name").asText());
        assertEquals("fr", listBody.get("content").get(0).get("language").asText());

        var detail = request("GET", "/api/v1/products/" + id, null, null, null);
        assertEquals(200, detail.statusCode());
        assertEquals("Travel kit", json(detail.body()).get("name").asText());

        var frenchDetail = request("GET", "/api/v1/products/" + id, null, null, "fr");
        assertEquals("Essentiels de voyage", json(frenchDetail.body()).get("description").asText());
    }

    @Test
    void versionTwoListingAndDetailsExposeBothTranslations() throws Exception {
        Product saved = saveProduct("V2-" + java.util.UUID.randomUUID(), "ACTIVE");
        String id = saved.getId().toString();

        var list = request("GET", "/api/v2/products?page=0&size=5", null, null, null);
        assertEquals(200, list.statusCode());
        JsonNode listItem = json(list.body()).get("content").get(0);
        assertEquals("Travel kit", listItem.get("translations").get("en").get("name").asText());
        assertEquals("Kit de voyage", listItem.get("translations").get("fr").get("name").asText());

        var detail = request("GET", "/api/v2/products/" + id, null, null, null);
        assertEquals(200, detail.statusCode());
        assertEquals("Voyage", json(detail.body()).get("translations").get("fr").get("category").asText());
    }

    @Test
    void publicListingValidatesPaginationAndDetailReturnsNotFound() throws Exception {
        var invalidSize = request("GET", "/api/v1/products?size=101", null, null, null);
        assertEquals(400, invalidSize.statusCode());

        String missingId = java.util.UUID.randomUUID().toString();
        assertEquals(404, request("GET", "/api/v1/products/" + missingId, null, null, null).statusCode());
        assertEquals(404, request("GET", "/api/v2/products/" + missingId, null, null, null).statusCode());
        assertTrue(productRepository.findAll().isEmpty());
    }
}
