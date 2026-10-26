package com.energymonitor.home.api;

import java.time.Instant;

/**
 * The owner of a home handed it over to one of its members.
 *
 * <p>Published inside the transaction that swaps the two roles. Nothing reacts to it yet and it is
 * not relayed to the broker; it is here so that can be added (for example, notifications telling
 * the new owner) without the use case having to know about it.
 *
 * @param homeId          the home
 * @param previousOwnerId the user who was the owner and is now a member
 * @param newOwnerId      the member who is now the owner
 * @param occurredAt      when the transfer happened
 */
public record HomeOwnershipTransferred(String homeId, String previousOwnerId, String newOwnerId,
                                       Instant occurredAt) {
}
