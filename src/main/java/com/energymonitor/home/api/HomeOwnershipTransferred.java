package com.energymonitor.home.api;

import java.time.Instant;
import java.util.List;

/**
 * The owner of a home handed it over to one of its members.
 *
 * <p>Published inside the transaction that swaps the two roles, and relayed to the broker as
 * {@code home.ownership-transferred}: the notification service tells the new owner, the previous
 * one and the other members. Also published when the account of the only owner is deleted and a
 * member inherits the home.
 *
 * @param homeId          the home
 * @param previousOwnerId the user who was the owner and is now a member
 * @param newOwnerId      the member who is now the owner
 * @param occurredAt      when the transfer happened
 */
public record HomeOwnershipTransferred(String homeId, String previousOwnerId, String newOwnerId,
                                       Instant occurredAt, String homeName,
                                       List<String> memberIds) {

    public HomeOwnershipTransferred {
        memberIds = List.copyOf(memberIds);
    }
}
