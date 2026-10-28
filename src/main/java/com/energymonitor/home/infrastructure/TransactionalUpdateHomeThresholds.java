package com.energymonitor.home.infrastructure;

import com.energymonitor.home.application.command.UpdateHomeThresholdsCommand;
import com.energymonitor.home.application.port.in.UpdateHomeThresholds;
import com.energymonitor.home.application.result.HomeThresholdsResult;
import com.energymonitor.home.application.usecase.UpdateHomeThresholdsService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs {@link UpdateHomeThresholdsService} in one transaction, so the saved thresholds and the
 * {@code home.thresholds-updated} message commit together or not at all. The service stays
 * framework-free; this decorator is the one the web layer receives ({@link Primary}).
 */
@Component
@Primary
public class TransactionalUpdateHomeThresholds implements UpdateHomeThresholds {

    private final UpdateHomeThresholdsService target;
    private final TransactionTemplate transaction;

    public TransactionalUpdateHomeThresholds(UpdateHomeThresholdsService target,
                                             PlatformTransactionManager transactions) {
        this.target = target;
        this.transaction = new TransactionTemplate(transactions);
    }

    @Override
    public HomeThresholdsResult update(UpdateHomeThresholdsCommand command) {
        return transaction.execute(status -> target.update(command));
    }

    @Override
    public HomeThresholdsResult resetToDefaults(String userId, String homeId) {
        return transaction.execute(status -> target.resetToDefaults(userId, homeId));
    }
}
