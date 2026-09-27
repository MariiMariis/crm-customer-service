package com.pb.crm.accounts.domain.company;

import java.util.regex.Pattern;

public record Cnpj(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9]{12}[0-9]{2}");
    private static final int[] FIRST_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("CNPJ deve conter 14 caracteres (12 alfanumericos e 2 digitos verificadores)");
        }
        if (value.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CNPJ invalido");
        }
        int first = checkDigit(value.substring(0, 12), FIRST_WEIGHTS);
        int second = checkDigit(value.substring(0, 12) + first, SECOND_WEIGHTS);
        if (value.charAt(12) - '0' != first || value.charAt(13) - '0' != second) {
            throw new IllegalArgumentException("CNPJ invalido: digitos verificadores nao conferem");
        }
    }

    public static Cnpj of(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("CNPJ e obrigatorio");
        }
        return new Cnpj(raw.replaceAll("[.\\-/\\s]", "").toUpperCase());
    }

    public static Cnpj fromBase(String base) {
        String normalized = base == null ? "" : base.toUpperCase();
        if (!normalized.matches("[A-Z0-9]{12}")) {
            throw new IllegalArgumentException("a base do CNPJ deve conter 12 caracteres alfanumericos");
        }
        int first = checkDigit(normalized, FIRST_WEIGHTS);
        int second = checkDigit(normalized + first, SECOND_WEIGHTS);
        return new Cnpj(normalized + first + second);
    }

    public String formatted() {
        return "%s.%s.%s/%s-%s".formatted(
                value.substring(0, 2),
                value.substring(2, 5),
                value.substring(5, 8),
                value.substring(8, 12),
                value.substring(12));
    }

    private static int checkDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (base.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @Override
    public String toString() {
        return formatted();
    }
}
