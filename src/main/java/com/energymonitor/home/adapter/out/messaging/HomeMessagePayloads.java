package com.energymonitor.home.adapter.out.messaging;

import java.time.Instant;
import java.util.List;

/**
 * The JSON bodies of the messages in {@link HomeMessageTypes}, one record per type.
 *
 * <p>They are the wire contract, kept apart from the {@code api} records on purpose: a domain
 * event can gain a field without every consumer having to understand it. Instants travel as
 * ISO-8601 strings.
 */
public final class HomeMessagePayloads {

    /** {@code home.created} */
    public record HomeCreatedPayload(String homeId, String homeName, String ownerId,
                                     Instant occurredAt) {
    }

    /** {@code home.member-joined}; {@code memberIds} includes the new member. */
    public record MemberJoinedPayload(String homeId, String homeName, String userId,
                                      List<String> memberIds, Instant occurredAt) {
    }

    /** {@code home.member-left}; {@code memberIds} are the members who stay. */
    public record MemberLeftPayload(String homeId, String homeName, String userId,
                                    List<String> memberIds, Instant occurredAt) {
    }

    /** {@code home.member-removed}; {@code memberIds} are the members who stay. */
    public record MemberRemovedPayload(String homeId, String homeName, String userId,
                                       String removedBy, List<String> memberIds,
                                       Instant occurredAt) {
    }

    /** {@code home.thresholds-updated} */
    public record ThresholdsUpdatedPayload(String homeId, String homeName, String updatedBy,
                                           List<String> memberIds, Instant occurredAt) {
    }

    /** {@code home.ownership-transferred} */
    public record OwnershipTransferredPayload(String homeId, String homeName,
                                              String previousOwnerId, String newOwnerId,
                                              List<String> memberIds, Instant occurredAt) {
    }

    /**
     * {@code home.deleted}. {@code homeName} and {@code memberIds} were added later: they are
     * read before the home is deleted, and a consumer that does not need them ignores them.
     */
    public record HomeDeletedPayload(String homeId, String deletedBy, Instant deletedAt,
                                     String homeName, List<String> memberIds) {
    }

    private HomeMessagePayloads() {
    }
}
