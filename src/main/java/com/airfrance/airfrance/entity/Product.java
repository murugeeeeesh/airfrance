package com.airfrance.airfrance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
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

    @Column(nullable = false)
    private int revision = 1;

    @Version
    private long entityVersion;

    protected Product() {
    }

    public Product(String sku, String nameEn, String nameFr, String descriptionEn,
                   String descriptionFr, String categoryEn, String categoryFr,
                   BigDecimal price, String currency, String status) {
        this.sku = sku;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.descriptionEn = descriptionEn;
        this.descriptionFr = descriptionFr;
        this.categoryEn = categoryEn;
        this.categoryFr = categoryFr;
        this.price = price;
        this.currency = currency;
        this.status = status;
    }

    public void update(String sku, String nameEn, String nameFr, String descriptionEn,
                       String descriptionFr, String categoryEn, String categoryFr,
                       BigDecimal price, String currency, String status) {
        this.sku = sku;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.descriptionEn = descriptionEn;
        this.descriptionFr = descriptionFr;
        this.categoryEn = categoryEn;
        this.categoryFr = categoryFr;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.revision++;
    }

    public UUID getId() { return id; }
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
    public int getRevision() { return revision; }
}
