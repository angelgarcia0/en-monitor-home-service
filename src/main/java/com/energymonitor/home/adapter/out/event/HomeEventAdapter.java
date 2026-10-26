package com.energymonitor.home.adapter.out.event;

import com.energymonitor.home.api.HomeDeleted;
import com.energymonitor.home.api.HomeOwnershipTransferred;
import com.energymonitor.home.application.port.out.HomeEventPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes home events to the Spring application context.
 *
 * <p>The use cases publish them inside the transaction of the change, so a synchronous listener
 * commits or rolls back with it. {@link HomeDeleted} is also what the device service needs, as
 * {@code home.deleted} on the broker: that outbox write comes with the messaging, and until then
 * this service takes no traffic, so energy-monitor-backend still announces every deletion.
 */
@Component
public class HomeEventAdapter implements HomeEventPort {

    private final ApplicationEventPublisher publisher;

    public HomeEventAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(HomeDeleted event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publish(HomeOwnershipTransferred event) {
        publisher.publishEvent(event);
    }
}
