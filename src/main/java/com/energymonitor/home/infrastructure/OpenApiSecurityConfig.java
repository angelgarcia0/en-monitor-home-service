package com.energymonitor.home.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the OpenAPI description and the one security scheme this API authenticates with: a
 * bearer JWT signed by the security service.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Energy Monitor Home API",
        version = "v1",
        description = "Homes and their members: creation, joining with the access code, "
                + "favourites, leaving, removing members, ownership transfer, consumption "
                + "thresholds and the home type catalogue. The bearer token is issued by the "
                + "security service."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiSecurityConfig {
}
