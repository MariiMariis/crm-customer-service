package com.pb.crm.sales.domain.lead;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record LeadScore(int total, List<ScoreFactor> factors) {

    public static final int MAX = 100;

    private static final List<String> EXECUTIVE_TITLES = List.of(
            "ceo", "cto", "cio", "cfo", "coo", "ciso", "diretor", "diretora", "director", "presidente", "president", "vp", "socio", "owner");
    private static final List<String> MANAGER_TITLES = List.of(
            "gerente", "manager", "head", "coordenador", "coordenadora", "coordinator", "supervisor", "supervisora", "lider");
    private static final BigDecimal HIGH_VALUE = new BigDecimal("500000");
    private static final BigDecimal MEDIUM_VALUE = new BigDecimal("100000");
    private static final BigDecimal LOW_VALUE = new BigDecimal("20000");

    public static LeadScore evaluate(LeadDetails details, LeadStatus status) {
        List<ScoreFactor> factors = new ArrayList<>();
        if (details.email() != null) {
            factors.add(new ScoreFactor("e-mail informado", 10));
        }
        if (details.phone() != null) {
            factors.add(new ScoreFactor("telefone informado", 10));
        }
        String title = normalize(details.jobTitle());
        if (containsAny(title, EXECUTIVE_TITLES)) {
            factors.add(new ScoreFactor("cargo executivo (C-level/diretoria)", 20));
        } else if (containsAny(title, MANAGER_TITLES)) {
            factors.add(new ScoreFactor("cargo de gestao", 10));
        }
        if (details.source() != null && details.source().scorePoints() > 0) {
            factors.add(new ScoreFactor("origem " + details.source(), details.source().scorePoints()));
        }
        BigDecimal value = details.estimatedValue();
        if (value != null) {
            if (value.compareTo(HIGH_VALUE) >= 0) {
                factors.add(new ScoreFactor("valor estimado >= R$ 500 mil", 25));
            } else if (value.compareTo(MEDIUM_VALUE) >= 0) {
                factors.add(new ScoreFactor("valor estimado >= R$ 100 mil", 15));
            } else if (value.compareTo(LOW_VALUE) >= 0) {
                factors.add(new ScoreFactor("valor estimado >= R$ 20 mil", 8));
            }
        }
        if (status == LeadStatus.CONTACTED) {
            factors.add(new ScoreFactor("lead contatado", 5));
        } else if (status == LeadStatus.QUALIFIED || status == LeadStatus.CONVERTING || status == LeadStatus.CONVERTED) {
            factors.add(new ScoreFactor("lead qualificado", 15));
        }
        int total = Math.min(MAX, factors.stream().mapToInt(ScoreFactor::points).sum());
        return new LeadScore(total, List.copyOf(factors));
    }

    private static boolean containsAny(String title, List<String> keywords) {
        if (title.isEmpty()) {
            return false;
        }
        for (String word : title.split("[^a-z]+")) {
            if (keywords.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
