package com.energymonitor.home.infrastructure;

import com.energymonitor.home.application.command.JoinHomeCommand;
import com.energymonitor.home.application.port.in.JoinHome;
import com.energymonitor.home.application.result.UserHomeResult;
import com.energymonitor.home.application.usecase.JoinHomeService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs {@link JoinHomeService} in one transaction, so the membership and the
 * {@code home.member-joined} message commit together or not at all. The service stays
 * framework-free; this decorator is the one the web layer receives ({@link Primary}).
 */
@Component
@Primary
public class TransactionalJoinHome implements JoinHome {

    private final JoinHomeService target;
    private final TransactionTemplate transaction;

    public TransactionalJoinHome(JoinHomeService target, PlatformTransactionManager transactions) {
        this.target = target;
        this.transaction = new TransactionTemplate(transactions);
    }

    @Override
    public UserHomeResult join(JoinHomeCommand command) {
        return transaction.execute(status -> target.join(command));
    }
}
