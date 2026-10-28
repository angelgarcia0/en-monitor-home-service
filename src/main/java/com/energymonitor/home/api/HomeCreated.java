package com.energymonitor.home.api;

import java.time.Instant;

/**
 * A user created a home and became its owner.
 *
 * <p>Published inside the transaction that creates it, and relayed to the broker as
 * {@code home.created}: the notification service tells the owner.
 *
 * @param homeId     the new home
 * @param homeName   its name
 * @param ownerId    the user who created it
 * @param occurredAt when it was created
 */
public record HomeCreated(String homeId, String homeName, String ownerId, Instant occurredAt) {
}
