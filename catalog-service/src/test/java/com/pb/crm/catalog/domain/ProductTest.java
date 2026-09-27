package com.pb.crm.catalog.domain;

import com.pb.crm.catalog.domain.product.BillingType;
import com.pb.crm.catalog.domain.product.Product;
import com.pb.crm.catalog.domain.product.ProductCategory;
import com.pb.crm.catalog.domain.product.ProductDetails;
import com.pb.crm.catalog.domain.product.ProductSubcategory;
import com.pb.crm.catalog.domain.product.Sku;
import com.pb.crm.catalog.domain.product.UnitOfMeasure;
import com.pb.crm.commons.error.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static ProductDetails notebook(BillingType billing, Integer warranty) {
        return new ProductDetails("Notebook Dell Latitude 5450", null, ProductSubcategory.NOTEBOOK, billing,
                UnitOfMeasure.UNIT, new BigDecimal("8500.00"), new BigDecimal("6800.00"), new BigDecimal("8.00"),
                "Dell", warranty);
    }

    private static ProductDetails subscription(ProductSubcategory subcategory, BillingType billing) {
        return new ProductDetails("Microsoft 365 Business Standard", null, subcategory, billing,
                UnitOfMeasure.USER, new BigDecimal("79.90"), new BigDecimal("62.00"), new BigDecimal("5.00"),
                "Microsoft", null);
    }

    @Test
    void skuIsGeneratedFromCategoryAndSubcategory() {
        assertThat(Sku.generate(ProductSubcategory.SECURITY, 1).value()).isEqualTo("SW-SEC-000001");
        assertThat(Sku.generate(ProductSubcategory.NOTEBOOK, 42).value()).isEqualTo("HW-NBK-000042");
        assertThat(Sku.generate(ProductSubcategory.IMPLEMENTATION, 999_999).value()).isEqualTo("SV-IMP-999999");
        assertThatThrownBy(() -> Sku.generate(ProductSubcategory.ERP, 1_000_000)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new Sku("XX-ABC-000001")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registerDerivesCategoryAndComputesMargin() {
        Product product = Product.register(Sku.generate(ProductSubcategory.NOTEBOOK, 7), notebook(BillingType.ONE_TIME, 36));

        assertThat(product.getCategory()).isEqualTo(ProductCategory.HARDWARE);
        assertThat(product.marginPercent()).isEqualByComparingTo("20.00");
        assertThat(product.isSellable()).isTrue();
    }

    @Test
    void billingMustMatchCategoryRules() {
        assertThatThrownBy(() -> Product.register(Sku.generate(ProductSubcategory.NOTEBOOK, 1), notebook(BillingType.MONTHLY, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("HARDWARE");
        assertThatThrownBy(() -> Product.register(Sku.generate(ProductSubcategory.SUPPORT, 1),
                subscription(ProductSubcategory.SUPPORT, BillingType.ANNUAL)))
                .isInstanceOf(BusinessRuleException.class);

        Product annual = Product.register(Sku.generate(ProductSubcategory.PRODUCTIVITY, 2),
                subscription(ProductSubcategory.PRODUCTIVITY, BillingType.ANNUAL));
        assertThat(annual.getDetails().billing().isRecurring()).isTrue();
    }

    @Test
    void warrantyOnlyForHardwareAndSkuMustMatchSubcategory() {
        ProductDetails softwareWithWarranty = new ProductDetails("Antivirus", null, ProductSubcategory.SECURITY,
                BillingType.ANNUAL, UnitOfMeasure.DEVICE, BigDecimal.TEN, null, BigDecimal.ZERO, null, 12);

        assertThatThrownBy(() -> Product.register(Sku.generate(ProductSubcategory.SECURITY, 3), softwareWithWarranty))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("garantia");
        assertThatThrownBy(() -> Product.register(Sku.generate(ProductSubcategory.SERVER, 3), notebook(BillingType.ONE_TIME, 12)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    void priceAndDiscountBoundsAreEnforced() {
        ProductDetails zeroPrice = new ProductDetails("Consultoria", null, ProductSubcategory.CONSULTING, BillingType.ONE_TIME,
                UnitOfMeasure.HOUR, BigDecimal.ZERO, null, BigDecimal.ZERO, null, null);
        ProductDetails excessiveDiscount = new ProductDetails("Consultoria", null, ProductSubcategory.CONSULTING,
                BillingType.ONE_TIME, UnitOfMeasure.HOUR, new BigDecimal("250"), null, new BigDecimal("100.01"), null, null);

        assertThatThrownBy(() -> Product.register(Sku.generate(ProductSubcategory.CONSULTING, 4), zeroPrice))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Product.register(Sku.generate(ProductSubcategory.CONSULTING, 5), excessiveDiscount))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void subcategoryIsImmutableAndArchivedProductIsNotSellable() {
        Product product = Product.register(Sku.generate(ProductSubcategory.NOTEBOOK, 8), notebook(BillingType.ONE_TIME, 12));
        ProductDetails desktop = new ProductDetails("Desktop", null, ProductSubcategory.DESKTOP, BillingType.ONE_TIME,
                UnitOfMeasure.UNIT, BigDecimal.TEN, null, BigDecimal.ZERO, null, null);

        assertThatThrownBy(() -> product.update(desktop)).isInstanceOf(BusinessRuleException.class);

        product.archive();
        assertThat(product.isActive()).isFalse();
        assertThat(product.isSellable()).isFalse();
    }
}
