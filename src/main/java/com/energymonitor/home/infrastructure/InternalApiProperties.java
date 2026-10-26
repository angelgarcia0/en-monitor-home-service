package com.energymonitor.home.infrastructure;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of the calls between this service and the others ({@code internal-api.*}).
 *
 * @param token              the secret the services share; sent as {@code X-Internal-Token} on
 *                           every internal call
 * @param securityServiceUrl where the security service answers its internal endpoints
 * @param connectTimeout     how long to wait for the connection
 * @param readTimeout        how long to wait for the answer
 */
@ConfigurationProperties("internal-api")
public record InternalApiProperties(
        String token,
        @DefaultValue("http://localhost:8082") URI securityServiceUrl,
        @DefaultValue("PT1S") Duration connectTimeout,
        @DefaultValue("PT2S") Duration readTimeout) {

    /** The header carrying {@link #token()}. */
    public static final String TOKEN_HEADER = "X-Internal-Token";
}
