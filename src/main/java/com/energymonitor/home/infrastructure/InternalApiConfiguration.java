package com.energymonitor.home.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security of the endpoints the other services call: the deletion check of the security service,
 * the home access check of the device and measurement services, the limits the measurement
 * service shows and the home energy-monitor-backend notifies the members of.
 *
 * <p>Those services present no JWT, so these paths get their own filter chain, evaluated before
 * the API chain: the caller must pass {@link InternalCallerCheck} (allowed network <em>and</em>
 * shared token). Behind a reverse proxy every request seems to come from the proxy, so the proxy
 * must not forward {@code /internal/**} at all.
 */
@Configuration(proxyBeanMethods = false)
public class InternalApiConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(InternalApiConfiguration.class);

    @Bean
    @Order(2)
    public SecurityFilterChain internalApiFilterChain(HttpSecurity http, InternalApiProperties properties)
            throws Exception {
        if (properties.token() == null || properties.token().isBlank()) {
            LOG.warn("internal-api.token is empty: every call to /internal/accounts/** and"
                    + " /internal/homes/** will be refused");
        }
        InternalCallerCheck callers = new InternalCallerCheck(properties);
        return http
                .securityMatcher("/internal/accounts/**", "/internal/homes/**")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .anyRequest().access((authentication, context) ->
                                new AuthorizationDecision(callers.trusts(context.getRequest()))))
                .build();
    }
}
