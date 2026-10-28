package com.energymonitor.home.infrastructure;

import com.energymonitor.home.adapter.out.security.HttpUserDirectoryAdapter;
import com.energymonitor.home.application.port.out.UserDirectoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * The client of the security service's internal API: the names of the users of a home.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InternalApiProperties.class)
public class SecurityServiceClientConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityServiceClientConfiguration.class);

    @Bean
    public UserDirectoryPort userDirectory(InternalApiProperties properties) {
        if (properties.token() == null || properties.token().isBlank()) {
            LOG.warn("internal-api.token is empty: the security service refuses every internal call");
        }
        SimpleClientHttpRequestFactory requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(properties.connectTimeout());
        requests.setReadTimeout(properties.readTimeout());
        RestClient client = RestClient.builder()
                .baseUrl(properties.securityServiceUrl().toString())
                .requestFactory(requests)
                .defaultHeader(InternalApiProperties.TOKEN_HEADER,
                        properties.token() == null ? "" : properties.token())
                .build();
        return new HttpUserDirectoryAdapter(client);
    }
}
