package com.airfrance.airfrance.service;

import com.airfrance.airfrance.entity.Product;
import com.airfrance.airfrance.repository.ProductRepository;
import com.airfrance.airfrance.web.ProductCatalogMapper;
import com.airfrance.airfrance.web.ProductResponse;
import com.airfrance.airfrance.web.ProductV2Response;
import com.airfrance.airfrance.web.RequestLanguage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCatalogMapper mapper;
    private final RequestLanguage requestLanguage;

    public ProductServiceImpl(ProductRepository productRepository, ProductCatalogMapper mapper,
                              RequestLanguage requestLanguage) {
        this.productRepository = productRepository;
        this.mapper = mapper;
        this.requestLanguage = requestLanguage;
    }

    @Override
    public Page<ProductResponse> getProducts(String search, String acceptLanguage, Pageable pageable) {
        String language = requestLanguage.resolve(acceptLanguage);
        return productRepository.searchCatalog(search, "ACTIVE", pageable)
                .map(product -> mapper.toLocalizedResponse(product, language));
    }

    @Override
    public ProductResponse getProductById(UUID id, String acceptLanguage) {
        Product product = productRepository.findById(id)
                .filter(candidate -> candidate.getStatus().equalsIgnoreCase("ACTIVE"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
        return mapper.toLocalizedResponse(product, requestLanguage.resolve(acceptLanguage));
    }

    @Override
    public Page<ProductV2Response> getProductsV2(String search, Pageable pageable) {
        return productRepository.searchCatalog(search, "ACTIVE", pageable).map(mapper::toV2Response);
    }

    @Override
    public ProductV2Response getProductByIdV2(UUID id) {
        Product product = productRepository.findById(id)
                .filter(candidate -> candidate.getStatus().equalsIgnoreCase("ACTIVE"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
        return mapper.toV2Response(product);
    }
}
