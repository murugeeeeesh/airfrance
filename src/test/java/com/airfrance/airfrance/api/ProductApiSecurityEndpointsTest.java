package com.airfrance.airfrance.api;

import com.airfrance.airfrance.entity.Product;
import tools.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductApiSecurityEndpointsTest extends AbstractProductApiIntegrationTest {

    @Test
    void authEndpointRejectsInvalidCredentialsAndMalformedPayloads() throws Exception {
        var invalidCredentials = request("POST", "/api/v1/auth/token",
                Map.of("username", "api-test-admin", "password", "wrong-password"), null, null);
        assertEquals(401, invalidCredentials.statusCode());

        var missingPassword = request("POST", "/api/v1/auth/token",
                Map.of("username", "api-test-admin"), null, null);
        assertEquals(400, missingPassword.statusCode());
    }

    @Test
    void everyAdminRouteRejectsMissingOrInvalidBearerTokens() throws Exception {
        Product product = saveProduct("SECURE-" + UUID.randomUUID(), "ACTIVE");
        String basePath = "/api/v1/admin/products";
        String productPath = basePath + "/" + product.getId();
        var writeRequest = productRequest(product.getSku(), "ACTIVE");

        assertEquals(401, request("GET", basePath, null, null, null).statusCode());
        assertEquals(401, request("POST", basePath, writeRequest, null, null).statusCode());
        assertEquals(401, request("PUT", productPath, writeRequest, null, null).statusCode());
        assertEquals(401, request("GET", productPath + "/revisions", null, null, null).statusCode());
        assertEquals(401, request("POST", productPath + "/revisions/1/restore",
                null, null, null).statusCode());

        assertEquals(401, request("GET", basePath, null, "not-a-valid-jwt", null).statusCode());
    }

    @Test
    void inactiveProductsAreHiddenFromBothPublicApiVersionsButVisibleToAdmin() throws Exception {
        Product inactive = saveProduct("INACTIVE-" + UUID.randomUUID(), "INACTIVE");
        String id = inactive.getId().toString();

        JsonNode v1Listing = json(request("GET", "/api/v1/products", null, null, null).body());
        assertEquals(0, v1Listing.get("totalElements").asInt());
        assertEquals(404, request("GET", "/api/v1/products/" + id, null, null, null).statusCode());
        JsonNode v2Listing = json(request("GET", "/api/v2/products", null, null, null).body());
        assertEquals(0, v2Listing.get("totalElements").asInt());
        assertEquals(404, request("GET", "/api/v2/products/" + id, null, null, null).statusCode());

        String token = adminToken();
        var adminListing = request("GET", "/api/v1/admin/products?status=INACTIVE", null, token, null);
        JsonNode adminBody = json(adminListing.body());
        assertEquals(200, adminListing.statusCode());
        assertEquals(1, adminBody.get("totalElements").asInt());
        assertEquals("INACTIVE", adminBody.get("content").get(0).get("status").asText());
    }
}
