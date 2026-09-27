package com.pb.crm.seeder;

public final class Cnpjs {

    private static final int[] FIRST_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Cnpjs() {
    }

    public static String fromBase(String base) {
        if (base == null || !base.matches("[0-9]{12}")) {
            throw new IllegalArgumentException("a base do CNPJ deve ter 12 digitos");
        }
        int first = checkDigit(base, FIRST_WEIGHTS);
        int second = checkDigit(base + first, SECOND_WEIGHTS);
        String digits = base + first + second;
        return "%s.%s.%s/%s-%s".formatted(digits.substring(0, 2), digits.substring(2, 5), digits.substring(5, 8),
                digits.substring(8, 12), digits.substring(12));
    }

    public static String forCompany(int sequence) {
        return fromBase("%08d0001".formatted(31_450_000 + sequence * 1_117));
    }

    private static int checkDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (base.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
