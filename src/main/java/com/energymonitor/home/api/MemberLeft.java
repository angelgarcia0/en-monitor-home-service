package com.energymonitor.home.api;

import java.time.Instant;
import java.util.List;

/**
 * A member left a home on their own.
 *
 * <p>Published inside the transaction that removes the membership, and relayed to the broker as
 * {@code home.member-left}: the notification service tells the members who stay.
 *
 * @param homeId     the home
 * @param homeName   its name
 * @param userId     the user who left
 * @param memberIds  the active members who stay, the owners included
 * @param occurredAt when the user left
 */
public record MemberLeft(String homeId, String homeName, String userId, List<String> memberIds,
                         Instant occurredAt) {

    public MemberLeft {
        memberIds = List.copyOf(memberIds);
    }
}
