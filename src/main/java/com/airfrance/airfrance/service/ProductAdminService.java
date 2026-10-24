package com.airfrance.airfrance.service;

import com.airfrance.airfrance.web.ProductRequest;
import com.airfrance.airfrance.web.ProductResponse;
import com.airfrance.airfrance.web.ProductRevisionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductAdminService {
    Page<ProductResponse> getProducts(String search, String status, String acceptLanguage, Pageable pageable);

    ProductResponse createProduct(ProductRequest request, String acceptLanguage);

    ProductResponse updateProduct(UUID id, ProductRequest request, String acceptLanguage);

    List<ProductRevisionResponse> getRevisions(UUID id, String acceptLanguage);

    ProductResponse restoreRevision(UUID id, int revision, String acceptLanguage);
}
