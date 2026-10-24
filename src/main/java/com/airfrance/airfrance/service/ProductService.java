package com.airfrance.airfrance.service;

import com.airfrance.airfrance.web.ProductResponse;
import com.airfrance.airfrance.web.ProductV2Response;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getProducts(String search, String acceptLanguage, Pageable pageable);

    ProductResponse getProductById(UUID id, String acceptLanguage);

    Page<ProductV2Response> getProductsV2(String search, Pageable pageable);

    ProductV2Response getProductByIdV2(UUID id);
}
