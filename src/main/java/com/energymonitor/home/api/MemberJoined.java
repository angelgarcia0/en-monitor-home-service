package com.energymonitor.home.api;

import java.time.Instant;
import java.util.List;

/**
 * A user joined a home with its access code.
 *
 * <p>Published inside the transaction that adds the membership, and relayed to the broker as
 * {@code home.member-joined}: the notification service tells the new member and the others.
 *
 * @param homeId     the home
 * @param homeName   its name
 * @param userId     the user who joined
 * @param memberIds  every active member after the join, the new one and the owners included
 * @param occurredAt when the user joined
 */
public record MemberJoined(String homeId, String homeName, String userId, List<String> memberIds,
                           Instant occurredAt) {

    public MemberJoined {
        memberIds = List.copyOf(memberIds);
    }
}
