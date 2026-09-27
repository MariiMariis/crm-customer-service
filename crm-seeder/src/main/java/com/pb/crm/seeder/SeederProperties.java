package com.pb.crm.seeder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "crm.seeder")
public record SeederProperties(
        @DefaultValue("http://localhost:8080") String gatewayUrl,
        @DefaultValue("carga-inicial") String actor,
        @DefaultValue("30s") Duration replicaWait
) {
}
