package com.airfrance.airfrance.repository;

import com.airfrance.airfrance.entity.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);

    @Query("""
            select p from Product p
            where (:search is null or :search = '' or
                   lower(p.sku) like lower(concat('%', :search, '%')) or
                   lower(p.categoryEn) like lower(concat('%', :search, '%')) or
                   lower(p.categoryFr) like lower(concat('%', :search, '%')) or
                   lower(p.nameEn) like lower(concat('%', :search, '%')) or
                   lower(p.nameFr) like lower(concat('%', :search, '%')))
              and (:status is null or :status = '' or lower(p.status) = lower(:status))
            """)
    Page<Product> searchCatalog(@Param("search") String search,
                                @Param("status") String status,
                                Pageable pageable);
}
