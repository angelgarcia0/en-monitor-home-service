package com.energymonitor.home.adapter.out.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.HomeCreatedPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.HomeDeletedPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.MemberJoinedPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.MemberLeftPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.MemberRemovedPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.OwnershipTransferredPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessagePayloads.ThresholdsUpdatedPayload;
import com.energymonitor.home.adapter.out.messaging.HomeMessageTypes;
import com.energymonitor.home.adapter.out.messaging.HomeOutboxWriter;
import com.energymonitor.home.api.HomeCreated;
import com.energymonitor.home.api.HomeDeleted;
import com.energymonitor.home.api.HomeOwnershipTransferred;
import com.energymonitor.home.api.MemberJoined;
import com.energymonitor.home.api.MemberLeft;
import com.energymonitor.home.api.MemberRemoved;
import com.energymonitor.home.api.ThresholdsUpdated;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Every home event is announced in memory and written to the outbox with its wire body: the
 * routing keys and the bodies are the contract with the device and notification services.
 */
@DisplayName("Home event adapter")
class HomeEventAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-23T15:00:00Z");
    private static final List<String> MEMBERS = List.of("use0000001", "use0000002");

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final HomeOutboxWriter outbox = mock(HomeOutboxWriter.class);
    private final HomeEventAdapter adapter = new HomeEventAdapter(publisher, outbox);

    @Test
    @DisplayName("a deleted home goes with its name and the members it had")
    void homeDeleted() {
        var event = new HomeDeleted("hom0000001", "use0000001", NOW, "Casa", MEMBERS);

        adapter.publish(event);

        verify(publisher).publishEvent(event);
        verify(outbox).write(HomeMessageTypes.HOME_DELETED,
                new HomeDeletedPayload("hom0000001", "use0000001", NOW, "Casa", MEMBERS), NOW);
    }

    @Test
    @DisplayName("an ownership transfer goes to the outbox too")
    void ownershipTransferred() {
        var event = new HomeOwnershipTransferred("hom0000001", "use0000001", "use0000002", NOW,
                "Casa", MEMBERS);

        adapter.publish(event);

        verify(publisher).publishEvent(event);
        verify(outbox).write(HomeMessageTypes.OWNERSHIP_TRANSFERRED, new OwnershipTransferredPayload(
                "hom0000001", "Casa", "use0000001", "use0000002", MEMBERS, NOW), NOW);
    }

    @Test
    @DisplayName("a new home, a member joining, leaving or removed and new thresholds go to the outbox")
    void activityEvents() {
        adapter.publish(new HomeCreated("hom0000001", "Casa", "use0000001", NOW));
        adapter.publish(new MemberJoined("hom0000001", "Casa", "use0000002", MEMBERS, NOW));
        adapter.publish(new MemberLeft("hom0000001", "Casa", "use0000002", List.of("use0000001"), NOW));
        adapter.publish(new MemberRemoved("hom0000001", "Casa", "use0000002", "use0000001",
                List.of("use0000001"), NOW));
        adapter.publish(new ThresholdsUpdated("hom0000001", "Casa", "use0000001", MEMBERS, NOW));

        verify(outbox).write(HomeMessageTypes.HOME_CREATED,
                new HomeCreatedPayload("hom0000001", "Casa", "use0000001", NOW), NOW);
        verify(outbox).write(HomeMessageTypes.MEMBER_JOINED,
                new MemberJoinedPayload("hom0000001", "Casa", "use0000002", MEMBERS, NOW), NOW);
        verify(outbox).write(HomeMessageTypes.MEMBER_LEFT, new MemberLeftPayload("hom0000001", "Casa",
                "use0000002", List.of("use0000001"), NOW), NOW);
        verify(outbox).write(HomeMessageTypes.MEMBER_REMOVED, new MemberRemovedPayload("hom0000001",
                "Casa", "use0000002", "use0000001", List.of("use0000001"), NOW), NOW);
        verify(outbox).write(HomeMessageTypes.THRESHOLDS_UPDATED,
                new ThresholdsUpdatedPayload("hom0000001", "Casa", "use0000001", MEMBERS, NOW), NOW);
    }
}
