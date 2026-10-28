package com.energymonitor.home.infrastructure;

import com.energymonitor.home.application.command.LeaveHomeCommand;
import com.energymonitor.home.application.port.in.LeaveHome;
import com.energymonitor.home.application.usecase.LeaveHomeService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs {@link LeaveHomeService} in one transaction, so the removed membership and the
 * {@code home.member-left} message commit together or not at all. The service stays
 * framework-free; this decorator is the one the web layer receives ({@link Primary}).
 */
@Component
@Primary
public class TransactionalLeaveHome implements LeaveHome {

    private final LeaveHomeService target;
    private final TransactionTemplate transaction;

    public TransactionalLeaveHome(LeaveHomeService target, PlatformTransactionManager transactions) {
        this.target = target;
        this.transaction = new TransactionTemplate(transactions);
    }

    @Override
    public void leave(LeaveHomeCommand command) {
        transaction.executeWithoutResult(status -> target.leave(command));
    }
}
