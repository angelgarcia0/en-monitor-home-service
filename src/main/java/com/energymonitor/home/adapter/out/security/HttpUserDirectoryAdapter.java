package com.energymonitor.home.adapter.out.security;

import com.energymonitor.home.application.port.out.UserDirectoryPort;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Asks the security service {@code POST /internal/users/profiles} who the users of a home are:
 * this service stores user identifiers only.
 *
 * <p>The names are only shown next to the members, so a security service that does not answer
 * within the timeouts does not fail the request: the members are listed without names, as
 * energy-monitor-backend did.
 */
public class HttpUserDirectoryAdapter implements UserDirectoryPort {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUserDirectoryAdapter.class);

    /** The most identifiers the security service accepts in one request. */
    static final int MAX_IDS_PER_REQUEST = 500;

    private final RestClient security;

    /**
     * @param security client with the security service's base URL, the timeouts and the token
     */
    public HttpUserDirectoryAdapter(RestClient security) {
        this.security = security;
    }

    @Override
    public Map<String, UserSummary> findByIds(Collection<String> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = userIds.stream().distinct().limit(MAX_IDS_PER_REQUEST).toList();
        try {
            Profiles body = security.post()
                    .uri("/internal/users/profiles")
                    .body(new ProfilesRequest(ids))
                    .retrieve()
                    .body(Profiles.class);
            return Optional.ofNullable(body).map(Profiles::profiles).orElse(List.of()).stream()
                    .collect(Collectors.toMap(Profile::userId,
                            profile -> new UserSummary(profile.name(), profile.lastName(),
                                    profile.email(), profile.profileImage()),
                            (first, second) -> first));
        } catch (RestClientException unavailable) {
            LOG.warn("Showing accounts without names: the security service did not answer the"
                    + " profiles request: {}", unavailable.getMessage());
            return Map.of();
        }
    }

    record ProfilesRequest(List<String> userIds) {
    }

    record Profiles(List<Profile> profiles) {
    }

    record Profile(String userId, String name, String lastName, String email,
                   String profileImage) {
    }
}
