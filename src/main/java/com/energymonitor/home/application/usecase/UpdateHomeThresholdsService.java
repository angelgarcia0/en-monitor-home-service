package com.energymonitor.home.application.usecase;

import com.energymonitor.home.application.port.out.SystemDefaultsPort;
import com.energymonitor.home.api.ThresholdsUpdated;
import com.energymonitor.home.application.command.UpdateHomeThresholdsCommand;
import com.energymonitor.home.application.exception.HomeAccessDeniedException;
import com.energymonitor.home.application.exception.HomeNotFoundException;
import com.energymonitor.home.application.port.in.UpdateHomeThresholds;
import com.energymonitor.home.application.port.out.HomeEventPort;
import com.energymonitor.home.application.port.out.HomePersistencePort;
import com.energymonitor.home.application.port.out.HomeThresholdsPersistencePort;
import com.energymonitor.home.application.port.out.UserHomePersistencePort;
import com.energymonitor.home.application.result.HomeThresholdsResult;
import com.energymonitor.home.domain.model.HomeThresholds;
import com.energymonitor.home.domain.model.UserHome;
import java.time.Clock;

/**
 * Updates the consumption thresholds of a home.
 *
 * <p>The user must be an active member with {@code canManageHome()} permission.
 * Validation errors from the domain ({@link IllegalArgumentException}) are not caught
 * here; they are mapped to HTTP 400 in the web layer.
 *
 * <p>Both a change and a reset to the defaults announce {@link ThresholdsUpdated};
 * {@code TransactionalUpdateHomeThresholds} runs each in one transaction with the thresholds.
 */
public class UpdateHomeThresholdsService implements UpdateHomeThresholds {

    private final HomeThresholdsPersistencePort thresholdsPort;
    private final UserHomePersistencePort userHomePort;
    private final SystemDefaultsPort defaults;
    private final HomePersistencePort homePort;
    private final HomeEventPort events;
    private final Clock clock;

    public UpdateHomeThresholdsService(HomeThresholdsPersistencePort thresholdsPort,
                                       UserHomePersistencePort userHomePort, SystemDefaultsPort defaults,
                                       HomePersistencePort homePort, HomeEventPort events, Clock clock) {
        this.thresholdsPort = thresholdsPort;
        this.userHomePort = userHomePort;
        this.defaults = defaults;
        this.homePort = homePort;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public HomeThresholdsResult update(UpdateHomeThresholdsCommand command) {
        // Verify membership (404 if not a member)
        UserHome membership = userHomePort.findActive(command.userId(), command.homeId())
                .orElseThrow(() -> new HomeNotFoundException(
                        "no active membership for user " + command.userId() + " in home " + command.homeId()));

        // Verify permission (403 if member without canManageHome)
        if (!membership.getPermissions().canManageHome()) {
            throw new HomeAccessDeniedException(
                    "user " + command.userId() + " cannot manage home " + command.homeId());
        }

        // Load thresholds
        HomeThresholds thresholds = thresholdsPort.findActiveByHomeId(command.homeId())
                .orElseThrow(() -> new HomeNotFoundException(
                        "no thresholds found for home " + command.homeId()));

        // Delegate validation to domain (IllegalArgumentException propagates to web layer)
        thresholds.setLimit(command.limitPeriod(), command.limit());
        thresholdsPort.save(thresholds);
        announce(command.homeId(), command.userId());

        return HomeThresholdsResult.from(thresholds, defaults);
    }

    @Override
    public HomeThresholdsResult resetToDefaults(String userId, String homeId) {
        UserHome membership = userHomePort.findActive(userId, homeId)
                .orElseThrow(() -> new HomeNotFoundException(
                        "no active membership for user " + userId + " in home " + homeId));
        if (!membership.getPermissions().canManageHome()) {
            throw new HomeAccessDeniedException("user " + userId + " cannot manage home " + homeId);
        }
        HomeThresholds thresholds = thresholdsPort.findActiveByHomeId(homeId)
                .orElseThrow(() -> new HomeNotFoundException("no thresholds found for home " + homeId));
        thresholds.resetToDefaults(defaults.defaultDailyLimit(), defaults.defaultMonthlyLimit());
        thresholdsPort.save(thresholds);
        announce(homeId, userId);
        return HomeThresholdsResult.from(thresholds, defaults);
    }

    private void announce(String homeId, String updatedBy) {
        events.publish(new ThresholdsUpdated(homeId, HomeMembers.name(homePort, homeId), updatedBy,
                HomeMembers.ids(userHomePort, homeId), clock.instant()));
    }
}
