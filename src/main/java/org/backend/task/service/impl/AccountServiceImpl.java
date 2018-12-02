package org.backend.task.service.impl;

import com.google.inject.Inject;
import lombok.NonNull;
import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.backend.task.dto.event.AccountStateEvent;
import org.backend.task.service.AccountService;
import org.backend.task.service.LockService;
import org.backend.task.service.db.DatabaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    private final DatabaseService databaseService;
    private final LockService lockService;

    @Inject
    public AccountServiceImpl(DatabaseService databaseService,
                              LockService lockService) {
        this.databaseService = databaseService;
        this.lockService = lockService;
    }

    @Override
    public Optional<Account> createAccount() {
        return databaseService.createAccount();
    }

    @Override
    public Optional<Account> createAccount(BigDecimal amount) {
        if (amount.signum() < 0) {
            return Optional.empty();
        }
        return databaseService.createAccount(amount);
    }

    @Override
    public Optional<Account> findById(Long accountId) {
        return databaseService.getAccount(accountId);
    }

    @Override
    public List<Account> findAll() {
        return databaseService.getAccountIds().stream().map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findAllByState(AccountState accountState) {
        return databaseService.getAccountIds().stream().map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get).filter(account -> account.getState() == accountState)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Account> close(@NonNull Long id) {
        try {
            return lockService.invokeConcurrently(() -> {
                    databaseService.setAccountState(id,AccountState.CLOSED);
                    return findById(id);
                    }, id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
