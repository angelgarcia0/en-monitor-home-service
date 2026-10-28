package com.energymonitor.home.application.usecase;

import com.energymonitor.home.application.port.out.HomePersistencePort;
import com.energymonitor.home.application.port.out.UserHomePersistencePort;
import com.energymonitor.home.domain.model.Home;
import com.energymonitor.home.domain.model.UserHome;
import java.util.List;

/**
 * What the home events carry about a home: its name and its active members. Read inside the use
 * case, so an event about a member who left, or a home being deleted, still names the people it
 * concerns.
 */
final class HomeMembers {

    private HomeMembers() {
    }

    /** Every active member of the home, the owners included. */
    static List<String> ids(UserHomePersistencePort userHomePort, String homeId) {
        return userHomePort.listActiveByHomeId(homeId).stream().map(UserHome::userId).toList();
    }

    /** The name of the home, or null when it no longer exists. */
    static String name(HomePersistencePort homePort, String homeId) {
        return homePort.findActive(homeId).map(Home::name).orElse(null);
    }
}
