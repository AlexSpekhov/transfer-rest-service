package org.backend.task.service.impl;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.backend.task.dto.*;
import org.backend.task.dto.event.TransferEvent;
import org.backend.task.service.AccountService;
import org.backend.task.service.LockService;
import org.backend.task.service.TransferService;
import org.backend.task.service.db.DatabaseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    private final DatabaseService databaseService;
    private final LockService lockService;

    @Inject
    public TransferServiceImpl(DatabaseService databaseService,
                               AccountService accountService,
                               LockService lockService) {
        this.databaseService = databaseService;
        this.accountService = accountService;
        this.lockService = lockService;
    }

    @Override
    public BigDecimal getBalance(Long accountId) throws Exception {
        Account account = checkAccountAvailability(accountId);
        return account.getBalance();
    }

    @Override
    public List<TransferEvent> getTransfers(Long accountId) throws Exception {
        checkAccountAvailability(accountId);
        return getTransfersByDate(accountId, null, null);
    }

    @Override
    public List<TransferEvent> getTransfersByDate(Long accountId, LocalDateTime from, LocalDateTime to) throws Exception {
        checkAccountAvailability(accountId);
        return databaseService.getTransferEventsHistory(accountId, from, to);
    }

    private Account checkAccountAvailability(Long accountId) throws Exception {
        Optional<Account> account = accountService.findById(accountId);
        if (!account.isPresent()) {
            log.error("Not found account {}", accountId);
            throw new RuntimeException(ErrorMessage.ACCOUNT_NOT_EXISTS.text);
        }
        if (account.get().getState().equals(AccountState.CLOSED)) {
            log.error("Account {} is not available", accountId);
            throw new RuntimeException(ErrorMessage.OPERATION_IS_NOT_AVAILABLE.text);
        }
        return account.get();
    }

    @Override
    public Optional<ErrorMessage> transfer(Long from, Long to, Transfer transfer) {
        try {
            return lockService.invokeConcurrently(() -> {
                if (transfer.getAmount().signum() < 0) {
                    return Optional.of(ErrorMessage.NON_POSITIVE_AMOUNT);
                }
                Account debitAccount = checkAccountAvailability(from);
                checkAccountAvailability(to);
                BigDecimal debitBalance = debitAccount.getBalance();
                if (Objects.isNull(debitBalance) || transfer.getAmount().compareTo(debitBalance) > 0) {
                    return Optional.of(ErrorMessage.INSUFFICIENT_FUNDS);
                }
                TransferEvent dTransferEvent = TransferEvent.builder()
                        .accountId(from)
                        .amount(transfer.getAmount())
                        .description(transfer.getDescription())
                        .dateTime(LocalDateTime.now())
                        .direction(TransferDirection.DEBIT)
                        .build();
                databaseService.addTransferEvent(from, dTransferEvent);

                TransferEvent cTransferEvent = TransferEvent.builder()
                        .accountId(to)
                        .amount(transfer.getAmount())
                        .description(transfer.getDescription())
                        .dateTime(LocalDateTime.now())
                        .direction(TransferDirection.CREDIT)
                        .build();
                databaseService.addTransferEvent(to, cTransferEvent);
                return Optional.empty();
            }, from, to);
        } catch (Exception e) {
            return Optional.of(ErrorMessage.SYSTEM_ERROR);
        }
    }
}
