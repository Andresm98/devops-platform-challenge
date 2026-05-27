package com.anax.devops.app.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        String apiKeyHeader,
        String jwtHeader,
        String apiKey,
        String jwtSecret,
        long jwtExpirationMs
) {
}