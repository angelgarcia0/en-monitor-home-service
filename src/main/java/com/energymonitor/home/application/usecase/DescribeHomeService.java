package com.energymonitor.home.application.usecase;

import com.energymonitor.home.application.port.in.DescribeHome;
import com.energymonitor.home.application.port.out.HomePersistencePort;
import com.energymonitor.home.application.port.out.UserHomePersistencePort;
import com.energymonitor.home.domain.model.UserHome;
import java.util.Optional;

/**
 * The home and its members, for another service. Takes the place of {@code findHome} and
 * {@code memberIds} of the in-process API energy-monitor-backend had.
 */
public class DescribeHomeService implements DescribeHome {

    private final HomePersistencePort homes;
    private final UserHomePersistencePort memberships;

    public DescribeHomeService(HomePersistencePort homes, UserHomePersistencePort memberships) {
        this.homes = homes;
        this.memberships = memberships;
    }

    @Override
    public Optional<HomeSummary> describe(String homeId) {
        return homes.findActive(homeId).map(home -> new HomeSummary(home.idHome(), home.name(),
                home.homeTypeId(), home.address(), home.creationDate(),
                memberships.listActiveByHomeId(homeId).stream().map(UserHome::userId).toList()));
    }
}
