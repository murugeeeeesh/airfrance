package com.airfrance.airfrance.repository;

import com.airfrance.airfrance.entity.ProductRevision;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRevisionRepository extends JpaRepository<ProductRevision, UUID> {
    List<ProductRevision> findByProductIdOrderByRevisionNumberDesc(UUID productId);

    Optional<ProductRevision> findByProductIdAndRevisionNumber(UUID productId, int revisionNumber);
}
