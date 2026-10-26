package com.energymonitor.home.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Input port for the services that need a home and who belongs to it, such as the notifications
 * of energy-monitor-backend writing to every member.
 */
public interface DescribeHome {

    /**
     * @param homeId the home
     * @return the home and its members; empty when it does not exist or was deleted
     */
    Optional<HomeSummary> describe(String homeId);

    /**
     * @param homeId       identifier
     * @param name         display name
     * @param homeTypeId   identifier of the home type
     * @param address      physical address
     * @param creationDate when the home was created
     * @param memberIds    the users with an active membership, owners included
     */
    record HomeSummary(String homeId, String name, String homeTypeId, String address,
                       Instant creationDate, List<String> memberIds) {
    }
}
