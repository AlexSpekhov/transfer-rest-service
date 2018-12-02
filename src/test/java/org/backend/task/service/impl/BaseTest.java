package org.backend.task.service.impl;

import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.backend.task.service.AccountService;
import org.backend.task.service.ContextService;
import org.backend.task.service.LockService;
import org.backend.task.service.TransferService;
import org.backend.task.service.db.DatabaseService;
import org.junit.Before;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class BaseTest {

    protected ContextService contextService;
    protected AccountService accountService;
    protected TransferService transferService;
    protected DatabaseService databaseService;
    protected LockService lockService;

    @Before
    public void setUp() {
        contextService = ContextService.INSTANCE;
        accountService = contextService.getAccountService();
        transferService = contextService.getTransferService();
        databaseService = contextService.getDatabaseService();
        lockService = contextService.getLockService();
    }

    Account createAccount(BigDecimal balance) {
        Optional<Account> account = Objects.isNull(balance) ? accountService.createAccount() : accountService.createAccount(balance);
        return account.orElseThrow(() -> new RuntimeException("Couldn't create account"));
    }

    Account closeAccount(Long accountId) {
        return accountService.close(accountId).orElseThrow(() -> new RuntimeException("Close failed"));
    }

    void checkBalance(BigDecimal balance, Optional<Account> account) {
        assertTrue(account.isPresent());
        assertEquals(balance, account.get().getBalance());
    }

    void checkState(AccountState state, Optional<Account> account) {
        assertTrue(account.isPresent());
        assertEquals(state, account.get().getState());
    }
}
