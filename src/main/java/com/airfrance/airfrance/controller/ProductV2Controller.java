package com.airfrance.airfrance.controller;

import com.airfrance.airfrance.service.ProductService;
import com.airfrance.airfrance.web.PageResponse;
import com.airfrance.airfrance.web.ProductV2Response;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/products")
@Validated
public class ProductV2Controller {

    private final ProductService productService;

    public ProductV2Controller(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductV2Response> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(productService.getProductsV2(search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sku"))));
    }

    @GetMapping("/{id}")
    public ProductV2Response getProductById(@PathVariable UUID id) {
        return productService.getProductByIdV2(id);
    }
}
