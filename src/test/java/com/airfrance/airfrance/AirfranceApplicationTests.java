package com.airfrance.airfrance;

import com.airfrance.airfrance.entity.Product;
import com.airfrance.airfrance.repository.ProductRepository;
import com.airfrance.airfrance.repository.ProductRevisionRepository;
import com.airfrance.airfrance.service.ProductService;
import com.airfrance.airfrance.web.ProductRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.admin.username=test-admin",
        "app.security.admin.password=test-only-password",
        "app.security.jwt-secret=test-jwt-secret-that-is-long-enough-for-hs256",
        "app.security.jwt-ttl-seconds=900"
})
@AutoConfigureMockMvc
class AirfranceApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductRevisionRepository revisionRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        revisionRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void productSearchFiltersAndMapsResults() {
        String sku = "TEST-" + UUID.randomUUID();
        productRepository.save(product(sku, "ACTIVE"));

        Page<com.airfrance.airfrance.web.ProductResponse> results =
                productService.getProducts(sku.toLowerCase(), "en", PageRequest.of(0, 20));

        assertEquals(1, results.getTotalElements());
        assertEquals(sku, results.getContent().get(0).sku());
        assertEquals("Book", results.getContent().get(0).name());
        assertEquals(new BigDecimal("19.99"), results.getContent().get(0).price());
    }

    @Test
    void publicV1LocalizesAndV2ReturnsBothTranslations() throws Exception {
        Product product = productRepository.save(product("BOOK-1", "ACTIVE"));

        mockMvc.perform(get("/api/v1/products/{id}", product.getId()).header("Accept-Language", "fr-CA,fr;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Livre"))
                .andExpect(jsonPath("$.language").value("fr"));

        mockMvc.perform(get("/api/v2/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translations.en.name").value("Book"))
                .andExpect(jsonPath("$.translations.fr.name").value("Livre"));
    }

    @Test
    void inactiveProductsAreHiddenFromPublicReads() throws Exception {
        Product product = productRepository.save(product("HIDDEN-1", "INACTIVE"));

        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v2/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void invalidAdminCredentialsDoNotIssueAToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "username", "test-admin",
                                "password", "incorrect-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateEditReviewAndRestoreProductRevisions() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(productRequest("AIR-1", "ACTIVE"))))
                .andExpect(status().isUnauthorized());

        String token = loginAndGetToken();
        MvcResult createdResult = mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(productRequest("AIR-1", "ACTIVE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revision").value(1))
                .andReturn();
        UUID id = UUID.fromString(objectMapper.readTree(createdResult.getResponse().getContentAsString())
                .get("id").asText());

        mockMvc.perform(put("/api/v1/admin/products/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(productRequest("AIR-1", "INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(2))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(get("/api/v1/admin/products/{id}/revisions", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].revision").value(2))
                .andExpect(jsonPath("$[0].changedBy").value("test-admin"));

        mockMvc.perform(post("/api/v1/admin/products/{id}/revisions/1/restore", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "username", "test-admin",
                                "password", "test-only-password"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("Bearer", response.get("tokenType").asText());
        return response.get("accessToken").asText();
    }

    private Product product(String sku, String status) {
        return new Product(sku, "Book", "Livre", "A book", "Un livre",
                "Books", "Livres", new BigDecimal("19.99"), "EUR", status);
    }

    private ProductRequest productRequest(String sku, String status) {
        return new ProductRequest(sku, "Air France flight", "Vol Air France",
                "English product details", "Description du produit",
                "Travel", "Voyage", new BigDecimal("49.95"), "EUR", status);
    }
}
