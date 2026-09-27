package com.pb.crm.sales.domain.lead;

import java.time.Instant;
import java.time.LocalDate;

public record ConversionRequest(
        String cnpj,
        String industry,
        String companySize,
        String city,
        String state,
        boolean createOpportunity,
        String opportunityTitle,
        LocalDate expectedCloseDate,
        Instant requestedAt
) {
    public ConversionRequest {
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ e obrigatorio para converter o lead");
        }
        if (industry == null || industry.isBlank()) {
            throw new IllegalArgumentException("segmento e obrigatorio para converter o lead");
        }
        if (companySize == null || companySize.isBlank()) {
            throw new IllegalArgumentException("porte e obrigatorio para converter o lead");
        }
        if (createOpportunity && (opportunityTitle == null || opportunityTitle.isBlank())) {
            throw new IllegalArgumentException("titulo da oportunidade e obrigatorio quando ela sera criada");
        }
        if (createOpportunity && expectedCloseDate == null) {
            throw new IllegalArgumentException("data prevista de fechamento e obrigatoria quando a oportunidade sera criada");
        }
        cnpj = cnpj.trim();
        industry = industry.trim().toUpperCase();
        companySize = companySize.trim().toUpperCase();
        state = state == null || state.isBlank() ? null : state.trim().toUpperCase();
        city = city == null || city.isBlank() ? null : city.trim();
        opportunityTitle = opportunityTitle == null || opportunityTitle.isBlank() ? null : opportunityTitle.trim();
        requestedAt = requestedAt == null ? Instant.now() : requestedAt;
    }
}
