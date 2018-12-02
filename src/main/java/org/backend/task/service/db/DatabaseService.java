package org.backend.task.service.db;

import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.backend.task.dto.event.AccountStateEvent;
import org.backend.task.dto.event.TransferEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DatabaseService {

    List<Long> getAccountIds();

    Optional<Account> createAccount();

    Optional<Account> createAccount(BigDecimal amount);

    Optional<Account> getAccount(Long id);

    void setAccountState(Long accountId, AccountState accountState);

    Optional<AccountStateEvent> getAccountStateEvent(Long accountId);

    void addTransferEvent(Long accountId, TransferEvent transferEvent);

    List<TransferEvent> getTransferEventsHistory(Long accountId, LocalDateTime from, LocalDateTime to);
}
