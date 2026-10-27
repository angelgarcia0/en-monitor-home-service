package com.energymonitor.home.api;

import java.time.Instant;
import java.util.List;

/**
 * The consumption thresholds of a home were changed, or set back to the defaults.
 *
 * <p>Published inside the transaction that saves them, and relayed to the broker as
 * {@code home.thresholds-updated}: the notification service tells the members.
 *
 * @param homeId     the home
 * @param homeName   its name
 * @param updatedBy  the user who changed them
 * @param memberIds  every active member, the owners included
 * @param occurredAt when they changed
 */
public record ThresholdsUpdated(String homeId, String homeName, String updatedBy,
                                List<String> memberIds, Instant occurredAt) {

    public ThresholdsUpdated {
        memberIds = List.copyOf(memberIds);
    }
}
