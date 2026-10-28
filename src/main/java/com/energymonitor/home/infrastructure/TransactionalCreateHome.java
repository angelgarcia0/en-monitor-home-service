package com.energymonitor.home.infrastructure;

import com.energymonitor.home.application.command.CreateHomeCommand;
import com.energymonitor.home.application.port.in.CreateHome;
import com.energymonitor.home.application.result.HomeResult;
import com.energymonitor.home.application.usecase.CreateHomeService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs {@link CreateHomeService} in one transaction.
 *
 * <p>Creating a home writes the home, its thresholds, the owner's membership and the
 * {@code home.created} message. A home half written would have no owner, and one announced without
 * existing would tell its owner about nothing. The service stays framework-free; this decorator is
 * the one the web layer receives ({@link Primary}).
 */
@Component
@Primary
public class TransactionalCreateHome implements CreateHome {

    private final CreateHomeService target;
    private final TransactionTemplate transaction;

    public TransactionalCreateHome(CreateHomeService target, PlatformTransactionManager transactions) {
        this.target = target;
        this.transaction = new TransactionTemplate(transactions);
    }

    @Override
    public HomeResult create(CreateHomeCommand command) {
        return transaction.execute(status -> target.create(command));
    }
}
