package com.airfrance.airfrance.web;

import com.airfrance.airfrance.entity.Product;
import com.airfrance.airfrance.entity.ProductRevision;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProductCatalogMapper {

    public ProductResponse toLocalizedResponse(Product product, String language) {
        ProductTranslation translation = translation(product, language);
        return new ProductResponse(product.getId(), product.getSku(), translation.name(),
                translation.description(), product.getPrice(), product.getCurrency(),
                translation.category(), product.getStatus(), language, product.getRevision());
    }

    public ProductV2Response toV2Response(Product product) {
        return new ProductV2Response(product.getId(), product.getSku(), product.getPrice(),
                product.getCurrency(), product.getStatus(), product.getRevision(),
                Map.of("en", new ProductTranslation(product.getNameEn(), product.getDescriptionEn(),
                                product.getCategoryEn()),
                        "fr", new ProductTranslation(product.getNameFr(), product.getDescriptionFr(),
                                product.getCategoryFr())));
    }

    public ProductRevisionResponse toRevisionResponse(ProductRevision revision, String language) {
        boolean french = language.equals("fr");
        return new ProductRevisionResponse(revision.getRevisionNumber(), revision.getChangedAt(),
                revision.getChangedBy(), revision.getSku(),
                french ? revision.getNameFr() : revision.getNameEn(),
                french ? revision.getDescriptionFr() : revision.getDescriptionEn(),
                french ? revision.getCategoryFr() : revision.getCategoryEn(),
                revision.getPrice(), revision.getCurrency(), revision.getStatus(), language);
    }

    private ProductTranslation translation(Product product, String language) {
        if (language.equals("fr")) {
            return new ProductTranslation(product.getNameFr(), product.getDescriptionFr(),
                    product.getCategoryFr());
        }
        return new ProductTranslation(product.getNameEn(), product.getDescriptionEn(),
                product.getCategoryEn());
    }
}
