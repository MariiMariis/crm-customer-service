package com.pb.crm.catalog.domain.product;

public enum ProductSubcategory {
    SECURITY(ProductCategory.SOFTWARE, "SEC"),
    PRODUCTIVITY(ProductCategory.SOFTWARE, "PRD"),
    ERP(ProductCategory.SOFTWARE, "ERP"),
    CRM(ProductCategory.SOFTWARE, "CRM"),
    INFRASTRUCTURE(ProductCategory.SOFTWARE, "INF"),
    DEVELOPMENT(ProductCategory.SOFTWARE, "DEV"),
    NOTEBOOK(ProductCategory.HARDWARE, "NBK"),
    DESKTOP(ProductCategory.HARDWARE, "DSK"),
    SERVER(ProductCategory.HARDWARE, "SRV"),
    NETWORK(ProductCategory.HARDWARE, "NET"),
    STORAGE(ProductCategory.HARDWARE, "STO"),
    PERIPHERAL(ProductCategory.HARDWARE, "PER"),
    CONSULTING(ProductCategory.SERVICE, "CON"),
    IMPLEMENTATION(ProductCategory.SERVICE, "IMP"),
    SUPPORT(ProductCategory.SERVICE, "SUP"),
    MANAGED_SERVICES(ProductCategory.SERVICE, "MSV"),
    TRAINING(ProductCategory.SERVICE, "TRN");

    private final ProductCategory category;
    private final String code;

    ProductSubcategory(ProductCategory category, String code) {
        this.category = category;
        this.code = code;
    }

    public ProductCategory category() {
        return category;
    }

    public String code() {
        return code;
    }
}
