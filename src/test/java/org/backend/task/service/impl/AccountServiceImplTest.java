package org.backend.task.service.impl;

import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class AccountServiceImplTest extends BaseTest {

    @Test
    public void createAccountWithoutBalance() throws Exception {
        Account account = createAccount(null);
        checkBalance(BigDecimal.ZERO, Optional.of(account));
        checkState(AccountState.OPEN, Optional.of(account));
    }

    @Test
    public void createAccountWithBalance() throws Exception {
        Account account = createAccount(BigDecimal.TEN);
        checkBalance(BigDecimal.TEN, Optional.of(account));
        checkState(AccountState.OPEN, Optional.of(account));
    }

    @Test
    public void close() throws Exception {
        Account account = createAccount(null);
        Account closedAccount = closeAccount(account.getId());
        checkState(AccountState.CLOSED, Optional.of(closedAccount));
    }

    @Test
    public void findById() throws Exception {
        BigDecimal balance = BigDecimal.valueOf(123456);
        Account createdAccount = createAccount(balance);
        Optional<Account> account = accountService.findById(createdAccount.getId());
        checkBalance(balance, account);
    }

    @Test
    public void findAll() throws Exception {
        final int numNewAccounts = 56;
        final int currNum = countAccounts();
        IntStream.range(0, numNewAccounts).forEach(value -> accountService.createAccount());
        List<Account> allAccounts = accountService.findAll();
        assertEquals(allAccounts.size(), numNewAccounts + currNum);
    }

    @Test
    public void findAllByState() throws Exception {
        final int numNewAccounts = 20;
        IntStream.range(0, numNewAccounts).forEach(value -> accountService.createAccount());
        List<Account> allOpenAccounts = accountService.findAll().stream().filter(account -> account.getState().equals(AccountState.OPEN)).collect(Collectors.toList());
        long closedAccounts = allOpenAccounts
                .stream().filter(account -> account.getId() % 2 == 0)
                .map(account -> accountService.close(account.getId())).count();
        assertEquals(accountService.findAllByState(AccountState.OPEN).size(), allOpenAccounts.size() - closedAccounts);
    }

    private int countAccounts() {
        return accountService.findAll().size();
    }

}