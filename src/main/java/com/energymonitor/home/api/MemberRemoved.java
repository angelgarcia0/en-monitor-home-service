package com.energymonitor.home.api;

import java.time.Instant;
import java.util.List;

/**
 * An owner removed a member from a home.
 *
 * <p>Published inside the transaction that removes the membership, and relayed to the broker as
 * {@code home.member-removed}: the notification service tells the removed user and the members who
 * stay.
 *
 * @param homeId     the home
 * @param homeName   its name
 * @param userId     the user who was removed
 * @param removedBy  the owner who removed them
 * @param memberIds  the active members who stay, the owners included
 * @param occurredAt when the user was removed
 */
public record MemberRemoved(String homeId, String homeName, String userId, String removedBy,
                            List<String> memberIds, Instant occurredAt) {

    public MemberRemoved {
        memberIds = List.copyOf(memberIds);
    }
}
