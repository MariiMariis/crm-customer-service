package com.pb.crm.catalog.domain.product;

import java.util.regex.Pattern;

public record Sku(String value) {

    private static final Pattern FORMAT = Pattern.compile("(SW|HW|SV)-[A-Z]{3}-\\d{6}");
    private static final long MAX_SEQUENCE = 999_999L;

    public Sku {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("SKU invalido: formato esperado PREFIXO-SUB-000000");
        }
    }

    public static Sku generate(ProductSubcategory subcategory, long sequence) {
        if (sequence < 1 || sequence > MAX_SEQUENCE) {
            throw new IllegalStateException("sequencia de SKU fora do intervalo permitido: " + sequence);
        }
        return new Sku("%s-%s-%06d".formatted(subcategory.category().skuPrefix(), subcategory.code(), sequence));
    }

    @Override
    public String toString() {
        return value;
    }
}
