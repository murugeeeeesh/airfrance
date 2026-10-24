package com.airfrance.airfrance.controller;

import com.airfrance.airfrance.service.ProductService;
import com.airfrance.airfrance.web.PageResponse;
import com.airfrance.airfrance.web.ProductResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sku"));
        return PageResponse.from(productService.getProducts(search, acceptLanguage, pageable));
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id,
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String acceptLanguage) {
        return productService.getProductById(id, acceptLanguage);
    }
}
