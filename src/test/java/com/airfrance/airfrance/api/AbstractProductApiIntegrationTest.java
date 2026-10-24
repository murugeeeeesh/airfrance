package com.airfrance.airfrance.api;

import com.airfrance.airfrance.entity.Product;
import com.airfrance.airfrance.repository.ProductRepository;
import com.airfrance.airfrance.repository.ProductRevisionRepository;
import com.airfrance.airfrance.web.ProductRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.security.admin.username=api-test-admin",
                "app.security.admin.password=api-test-password",
                "app.security.jwt-secret=api-test-jwt-secret-with-at-least-32-bytes",
                "app.security.jwt-ttl-seconds=900"
        })
abstract class AbstractProductApiIntegrationTest {

    @LocalServerPort
    protected int port;

    protected final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected ProductRevisionRepository revisionRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void clearProducts() throws Exception {
        revisionRepository.deleteAll();
        productRepository.deleteAll();
    }

    protected Product saveProduct(String sku, String status) {
        return productRepository.save(new Product(sku, "Travel kit", "Kit de voyage",
                "Travel essentials", "Essentiels de voyage", "Travel", "Voyage",
                new BigDecimal("49.95"), "EUR", status));
    }

    protected ProductRequest productRequest(String sku, String status) {
        return new ProductRequest(sku, "Travel kit", "Kit de voyage",
                "Travel essentials", "Essentiels de voyage", "Travel", "Voyage",
                new BigDecimal("49.95"), "EUR", status);
    }

    protected String adminToken() throws Exception {
        HttpResponse<String> response = request("POST", "/api/v1/auth/token",
                Map.of("username", "api-test-admin", "password", "api-test-password"), null, null);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new AssertionError("Admin login failed with HTTP " + response.statusCode());
        }
        return json(response.body()).get("accessToken").asText();
    }

    protected HttpResponse<String> request(String method, String path, Object body,
                                           String token, String language) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (token != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (language != null) {
            builder.header(HttpHeaders.ACCEPT_LANGUAGE, language);
        }
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    protected JsonNode json(String body) throws JacksonException {
        return objectMapper.readTree(body);
    }
}
