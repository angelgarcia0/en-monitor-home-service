package com.energymonitor.home.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("Account deleted queue")
class HomeMessagingConfigurationTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(HomeMessagingConfiguration.class)
            .withPropertyValues(
                    "messaging.security-exchange=energy-monitor.security",
                    "messaging.dead-letter-exchange=home-service.dlx");

    @Test
    @DisplayName("not declared while energy-monitor-backend still consumes account.deleted")
    void offByDefault() {
        context.run(started -> assertThat(started).doesNotHaveBean(Declarables.class));
    }

    @Test
    @DisplayName("declared, bound to account.deleted and dead-lettered once switched on")
    void declaredWhenEnabled() {
        context.withPropertyValues("messaging.account-deleted.enabled=true").run(started -> {
            Declarables declarables = started.getBean(Declarables.class);

            assertThat(declarables.getDeclarablesByType(Queue.class))
                    .extracting(Queue::getName)
                    .containsExactlyInAnyOrder(HomeMessagingConfiguration.ACCOUNT_DELETED_QUEUE,
                            HomeMessagingConfiguration.ACCOUNT_DELETED_QUEUE + ".dlq");
            assertThat(declarables.getDeclarablesByType(Binding.class))
                    .anySatisfy(binding -> {
                        assertThat(binding.getExchange()).isEqualTo("energy-monitor.security");
                        assertThat(binding.getRoutingKey()).isEqualTo("account.deleted");
                    });
        });
    }
}
