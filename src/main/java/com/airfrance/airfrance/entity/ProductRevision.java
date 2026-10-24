package com.airfrance.airfrance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_revisions", uniqueConstraints =
        @UniqueConstraint(columnNames = {"product_id", "revision_number"}))
public class ProductRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(nullable = false, length = 100)
    private String changedBy;

    @Column(nullable = false)
    private String sku;
    @Column(nullable = false, length = 200)
    private String nameEn;
    @Column(nullable = false, length = 200)
    private String nameFr;
    @Column(nullable = false, length = 4000)
    private String descriptionEn;
    @Column(nullable = false, length = 4000)
    private String descriptionFr;
    @Column(nullable = false, length = 120)
    private String categoryEn;
    @Column(nullable = false, length = 120)
    private String categoryFr;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false, length = 20)
    private String status;

    protected ProductRevision() {
    }

    private ProductRevision(Product product, String changedBy) {
        this.productId = product.getId();
        this.revisionNumber = product.getRevision();
        this.changedAt = Instant.now();
        this.changedBy = changedBy;
        this.sku = product.getSku();
        this.nameEn = product.getNameEn();
        this.nameFr = product.getNameFr();
        this.descriptionEn = product.getDescriptionEn();
        this.descriptionFr = product.getDescriptionFr();
        this.categoryEn = product.getCategoryEn();
        this.categoryFr = product.getCategoryFr();
        this.price = product.getPrice();
        this.currency = product.getCurrency();
        this.status = product.getStatus();
    }

    public static ProductRevision snapshot(Product product, String changedBy) {
        return new ProductRevision(product, changedBy);
    }

    public void applyTo(Product product) {
        product.update(sku, nameEn, nameFr, descriptionEn, descriptionFr,
                categoryEn, categoryFr, price, currency, status);
    }

    public UUID getProductId() { return productId; }
    public int getRevisionNumber() { return revisionNumber; }
    public Instant getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
    public String getSku() { return sku; }
    public String getNameEn() { return nameEn; }
    public String getNameFr() { return nameFr; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionFr() { return descriptionFr; }
    public String getCategoryEn() { return categoryEn; }
    public String getCategoryFr() { return categoryFr; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
}
