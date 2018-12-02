package org.backend.task.service;

import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.backend.task.dto.event.AccountStateEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {

    Optional<Account> createAccount();

    Optional<Account> createAccount(BigDecimal amount);

    Optional<Account> findById(Long accountId);

    List<Account> findAll();

    List<Account> findAllByState(AccountState accountState);

    Optional<Account> close(Long id);

}
