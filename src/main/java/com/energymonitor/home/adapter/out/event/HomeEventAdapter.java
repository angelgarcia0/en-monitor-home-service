package com.energymonitor.home.adapter.out.event;

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
import com.energymonitor.home.application.port.out.HomeEventPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes home events to the Spring application context and to the outbox, for the other
 * services.
 *
 * <p>The use cases publish them inside their transaction, so the outbox row commits with the
 * change it announces. The device service hears {@code home.deleted} and unlinks the home's
 * modules; the notification service hears every one of them and tells the people concerned.
 */
@Component
public class HomeEventAdapter implements HomeEventPort {

    private final ApplicationEventPublisher publisher;
    private final HomeOutboxWriter outbox;

    public HomeEventAdapter(ApplicationEventPublisher publisher, HomeOutboxWriter outbox) {
        this.publisher = publisher;
        this.outbox = outbox;
    }

    @Override
    public void publish(HomeCreated event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.HOME_CREATED, new HomeCreatedPayload(event.homeId(),
                event.homeName(), event.ownerId(), event.occurredAt()), event.occurredAt());
    }

    @Override
    public void publish(MemberJoined event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.MEMBER_JOINED, new MemberJoinedPayload(event.homeId(),
                event.homeName(), event.userId(), event.memberIds(), event.occurredAt()),
                event.occurredAt());
    }

    @Override
    public void publish(MemberLeft event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.MEMBER_LEFT, new MemberLeftPayload(event.homeId(),
                event.homeName(), event.userId(), event.memberIds(), event.occurredAt()),
                event.occurredAt());
    }

    @Override
    public void publish(MemberRemoved event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.MEMBER_REMOVED, new MemberRemovedPayload(event.homeId(),
                event.homeName(), event.userId(), event.removedBy(), event.memberIds(),
                event.occurredAt()), event.occurredAt());
    }

    @Override
    public void publish(ThresholdsUpdated event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.THRESHOLDS_UPDATED, new ThresholdsUpdatedPayload(event.homeId(),
                event.homeName(), event.updatedBy(), event.memberIds(), event.occurredAt()),
                event.occurredAt());
    }

    @Override
    public void publish(HomeDeleted event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.HOME_DELETED, new HomeDeletedPayload(event.homeId(),
                event.deletedBy(), event.occurredAt(), event.homeName(), event.memberIds()),
                event.occurredAt());
    }

    @Override
    public void publish(HomeOwnershipTransferred event) {
        publisher.publishEvent(event);
        outbox.write(HomeMessageTypes.OWNERSHIP_TRANSFERRED, new OwnershipTransferredPayload(
                event.homeId(), event.homeName(), event.previousOwnerId(), event.newOwnerId(),
                event.memberIds(), event.occurredAt()), event.occurredAt());
    }
}
