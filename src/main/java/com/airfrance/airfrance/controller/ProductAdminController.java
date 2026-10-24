package com.airfrance.airfrance.controller;

import com.airfrance.airfrance.service.ProductAdminService;
import com.airfrance.airfrance.web.PageResponse;
import com.airfrance.airfrance.web.ProductRequest;
import com.airfrance.airfrance.web.ProductResponse;
import com.airfrance.airfrance.web.ProductRevisionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
@Validated
public class ProductAdminController {

    private final ProductAdminService productAdminService;

    public ProductAdminController(ProductAdminService productAdminService) {
        this.productAdminService = productAdminService;
    }

    @GetMapping
    public PageResponse<ProductResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        return PageResponse.from(productAdminService.getProducts(search, status, acceptLanguage,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sku"))));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        ProductResponse created = productAdminService.createProduct(request, acceptLanguage);
        return ResponseEntity.created(URI.create("/api/v1/admin/products/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        return productAdminService.updateProduct(id, request, acceptLanguage);
    }

    @GetMapping("/{id}/revisions")
    public List<ProductRevisionResponse> getRevisions(@PathVariable UUID id,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        return productAdminService.getRevisions(id, acceptLanguage);
    }

    @PostMapping("/{id}/revisions/{revision}/restore")
    public ProductResponse restoreRevision(@PathVariable UUID id, @PathVariable @Min(1) int revision,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        return productAdminService.restoreRevision(id, revision, acceptLanguage);
    }
}
