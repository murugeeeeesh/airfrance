package com.airfrance.airfrance.api;

import com.airfrance.airfrance.entity.Product;
import tools.jackson.databind.JsonNode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminProductEndpointsTest extends AbstractProductApiIntegrationTest {

    @Test
    void adminCanCreateEditListInspectAndRestoreProductRevision() throws Exception {
        String token = adminToken();
        String sku = "ADMIN-" + UUID.randomUUID();
        String basePath = "/api/v1/admin/products";

        var created = request("POST", basePath, productRequest(sku, "ACTIVE"), token, null);
        assertEquals(201, created.statusCode());
        String id = json(created.body()).get("id").asText();
        assertEquals(1, json(created.body()).get("revision").asInt());

        var listed = request("GET", basePath + "?status=ACTIVE&search=" + sku, null, token, null);
        assertEquals(200, listed.statusCode());
        assertEquals(1, json(listed.body()).get("totalElements").asInt());

        var edited = request("PUT", basePath + "/" + id, productRequest(sku, "INACTIVE"), token, null);
        assertEquals(200, edited.statusCode());
        assertEquals(2, json(edited.body()).get("revision").asInt());
        assertEquals("INACTIVE", productRepository.findById(UUID.fromString(id)).orElseThrow().getStatus());

        var revisions = request("GET", basePath + "/" + id + "/revisions", null, token, "fr");
        JsonNode revisionsBody = json(revisions.body());
        assertEquals(200, revisions.statusCode());
        assertEquals(2, revisionsBody.size());
        assertEquals("Kit de voyage", revisionsBody.get(0).get("name").asText());
        assertEquals("api-test-admin", revisionsBody.get(0).get("changedBy").asText());

        var restored = request("POST", basePath + "/" + id + "/revisions/1/restore",
                null, token, null);
        assertEquals(200, restored.statusCode());
        assertEquals(3, json(restored.body()).get("revision").asInt());
        assertEquals("ACTIVE", json(restored.body()).get("status").asText());

        Product persisted = productRepository.findById(UUID.fromString(id)).orElseThrow();
        assertEquals("ACTIVE", persisted.getStatus());
        assertEquals(3, revisionRepository.findByProductIdOrderByRevisionNumberDesc(persisted.getId()).size());
    }

    @Test
    void adminWritesValidateRequestsAndRejectDuplicateSkus() throws Exception {
        String token = adminToken();
        String basePath = "/api/v1/admin/products";
        String sku = "DUP-" + UUID.randomUUID();
        var created = request("POST", basePath, productRequest(sku, "ACTIVE"), token, null);
        assertEquals(201, created.statusCode());

        assertEquals(409, request("POST", basePath,
                productRequest(sku, "ACTIVE"), token, null).statusCode());

        var invalid = productRequest("INVALID-" + UUID.randomUUID(), "UNKNOWN");
        assertEquals(400, request("POST", basePath, invalid, token, null).statusCode());
        assertEquals(1, productRepository.count());
    }

    @Test
    void adminRevisionAndProductRoutesReportMissingRecords() throws Exception {
        String token = adminToken();
        String missingId = UUID.randomUUID().toString();
        String basePath = "/api/v1/admin/products/" + missingId;

        assertEquals(404, request("PUT", basePath, productRequest("MISSING", "ACTIVE"),
                token, null).statusCode());
        assertEquals(404, request("GET", basePath + "/revisions",
                null, token, null).statusCode());
        assertEquals(404, request("POST", basePath + "/revisions/1/restore",
                null, token, null).statusCode());
    }
}
