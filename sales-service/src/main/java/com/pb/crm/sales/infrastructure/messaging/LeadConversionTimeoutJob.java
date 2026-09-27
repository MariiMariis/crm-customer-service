package com.pb.crm.sales.infrastructure.messaging;

import com.pb.crm.sales.application.lead.LeadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class LeadConversionTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(LeadConversionTimeoutJob.class);

    private final LeadService leadService;
    private final Duration timeout;

    public LeadConversionTimeoutJob(LeadService leadService,
                                    @Value("${crm.sales.conversion-timeout:15m}") Duration timeout) {
        this.leadService = leadService;
        this.timeout = timeout;
    }

    @Scheduled(fixedDelayString = "${crm.sales.conversion-timeout-check:60s}", initialDelayString = "${crm.sales.conversion-timeout-check:60s}")
    public void expireStaleConversions() {
        int expired = leadService.expireStaleConversions(Instant.now().minus(timeout));
        if (expired > 0) {
            log.warn("{} conversao(oes) de lead expirada(s) por timeout de {}", expired, timeout);
        }
    }
}
