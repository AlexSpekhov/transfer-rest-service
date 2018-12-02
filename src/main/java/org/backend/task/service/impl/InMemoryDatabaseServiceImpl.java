package org.backend.task.service.impl;

import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.backend.task.dto.TransferDirection;
import org.backend.task.dto.event.AccountStateEvent;
import org.backend.task.dto.event.TransferEvent;
import org.backend.task.service.db.DatabaseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryDatabaseServiceImpl implements DatabaseService {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final Map<Long, ConcurrentLinkedDeque<AccountStateEvent>> accountEvents = new ConcurrentHashMap<>();
    private final Map<Long, ConcurrentLinkedDeque<TransferEvent>> transferEvents = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong();

    @Override
    public List<Long> getAccountIds() {
        return accounts.keySet().stream().collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public Optional<Account> getAccount(Long id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Optional<Account> createAccount() {
        return createAccount(BigDecimal.ZERO);
    }

    @Override
    public Optional<Account> createAccount(BigDecimal amount) {
        Long id = idGenerator.incrementAndGet();
        Account account = new Account(id, AccountState.OPEN, amount);
        AccountStateEvent accountStateEvent = new AccountStateEvent(id, AccountState.OPEN);
        accounts.put(id, account);
        accountEvents.computeIfAbsent(id, element -> new ConcurrentLinkedDeque<>()).add(accountStateEvent);
        return Optional.of(account);
    }

    @Override
    public void setAccountState(Long accountId, AccountState accountState) {
        AccountStateEvent accountStateEvent = new AccountStateEvent(accountId, accountState);
        accountEvents.computeIfAbsent(accountId, element -> new ConcurrentLinkedDeque<>()).add(accountStateEvent);
        accounts.get(accountId).setState(accountState);
    }

    @Override
    public Optional<AccountStateEvent> getAccountStateEvent(Long accountId) {
        return Optional.ofNullable(accountEvents.get(accountId)).map(Deque::getLast);
    }

    @Override
    public void addTransferEvent(Long accountId, TransferEvent transferEvent) {
        Account account = accounts.get(accountId);
        if (transferEvent.getDirection().equals(TransferDirection.DEBIT)) {
            account.setBalance(account.getBalance().add(transferEvent.getAmount().negate()));
        } else {
            account.setBalance(account.getBalance().add(transferEvent.getAmount()));
        }
        transferEvents.computeIfAbsent(accountId, element -> new ConcurrentLinkedDeque<>()).addLast(transferEvent);
    }

    @Override
    public List<TransferEvent> getTransferEventsHistory(Long accountId, LocalDateTime from, LocalDateTime to) {
        Stream<TransferEvent> stream = transferEvents.getOrDefault(accountId, new ConcurrentLinkedDeque<>()).stream();
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            stream = stream.filter(transferEvent ->
                    transferEvent.getDateTime().compareTo(from) >= 0 && transferEvent.getDateTime().compareTo(to) <= 0);
        } else if (Objects.nonNull(from)) {
            stream = stream.filter(transferEvent -> transferEvent.getDateTime().compareTo(from) >= 0);
        } else if (Objects.nonNull(to)){
            stream = stream.filter(transferEvent -> transferEvent.getDateTime().compareTo(to) <= 0);
        }

        return stream.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
