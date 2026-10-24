package com.airfrance.airfrance.service;

import com.airfrance.airfrance.entity.Product;
import com.airfrance.airfrance.entity.ProductRevision;
import com.airfrance.airfrance.repository.ProductRepository;
import com.airfrance.airfrance.repository.ProductRevisionRepository;
import com.airfrance.airfrance.web.ProductCatalogMapper;
import com.airfrance.airfrance.web.ProductRequest;
import com.airfrance.airfrance.web.ProductResponse;
import com.airfrance.airfrance.web.ProductRevisionResponse;
import com.airfrance.airfrance.web.RequestLanguage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ProductAdminServiceImpl implements ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductRevisionRepository revisionRepository;
    private final ProductCatalogMapper mapper;
    private final RequestLanguage requestLanguage;

    public ProductAdminServiceImpl(ProductRepository productRepository,
                                   ProductRevisionRepository revisionRepository,
                                   ProductCatalogMapper mapper,
                                   RequestLanguage requestLanguage) {
        this.productRepository = productRepository;
        this.revisionRepository = revisionRepository;
        this.mapper = mapper;
        this.requestLanguage = requestLanguage;
    }

    @Override
    public Page<ProductResponse> getProducts(String search, String status, String acceptLanguage,
                                             Pageable pageable) {
        return productRepository.searchCatalog(search, status, pageable)
                .map(product -> mapper.toLocalizedResponse(product, requestLanguage.resolve(acceptLanguage)));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String acceptLanguage) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this SKU already exists");
        }
        Product product = productRepository.save(new Product(request.sku(), request.nameEn(), request.nameFr(),
                request.descriptionEn(), request.descriptionFr(), request.categoryEn(), request.categoryFr(),
                request.price(), request.currency(), request.status()));
        revisionRepository.save(ProductRevision.snapshot(product, currentAdmin()));
        return mapper.toLocalizedResponse(product, requestLanguage.resolve(acceptLanguage));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request, String acceptLanguage) {
        Product product = findProduct(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.sku(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this SKU already exists");
        }
        product.update(request.sku(), request.nameEn(), request.nameFr(), request.descriptionEn(),
                request.descriptionFr(), request.categoryEn(), request.categoryFr(), request.price(),
                request.currency(), request.status());
        Product savedProduct = productRepository.save(product);
        revisionRepository.save(ProductRevision.snapshot(savedProduct, currentAdmin()));
        return mapper.toLocalizedResponse(savedProduct, requestLanguage.resolve(acceptLanguage));
    }

    @Override
    public List<ProductRevisionResponse> getRevisions(UUID id, String acceptLanguage) {
        findProduct(id);
        String language = requestLanguage.resolve(acceptLanguage);
        return revisionRepository.findByProductIdOrderByRevisionNumberDesc(id).stream()
                .map(revision -> mapper.toRevisionResponse(revision, language))
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse restoreRevision(UUID id, int revision, String acceptLanguage) {
        Product product = findProduct(id);
        ProductRevision previous = revisionRepository.findByProductIdAndRevisionNumber(id, revision)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product revision not found: " + revision));
        previous.applyTo(product);
        Product savedProduct = productRepository.save(product);
        revisionRepository.save(ProductRevision.snapshot(savedProduct, currentAdmin()));
        return mapper.toLocalizedResponse(savedProduct, requestLanguage.resolve(acceptLanguage));
    }

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    private String currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
