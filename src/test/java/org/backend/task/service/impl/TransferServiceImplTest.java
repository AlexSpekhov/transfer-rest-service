package org.backend.task.service.impl;

import org.backend.task.dto.Account;
import org.backend.task.dto.ErrorMessage;
import org.backend.task.dto.Transfer;
import org.backend.task.dto.event.TransferEvent;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class TransferServiceImplTest extends BaseTest {

    private Account unlimitedAccount;

    @Before
    public void createUnlimitedAccount() {
        unlimitedAccount = createAccount(BigDecimal.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void getBalanceSuccess() throws Exception {
        BigDecimal amount = BigDecimal.TEN;
        Account account = createAccount(amount);
        BigDecimal balance = transferService.getBalance(account.getId());
        assertEquals(balance, amount);
    }

    @Test
    public void getBalanceFail() throws Exception {
        BigDecimal amount = BigDecimal.TEN;
        Account account = createAccount(amount);
        accountService.close(account.getId());
        try {
            transferService.getBalance(account.getId());
        } catch (Exception ex) {
            assertEquals(ex.getClass(), RuntimeException.class);
            assertEquals(ex.getMessage(), ErrorMessage.OPERATION_IS_NOT_AVAILABLE.text);
        }
    }

    @Test
    public void getTransfersFail1() throws Exception {
        BigDecimal amount = BigDecimal.TEN;
        Account account = createAccount(amount);
        accountService.close(account.getId());
        try {
            transferService.getTransfers(account.getId());
        } catch (Exception ex) {
            assertEquals(ex.getClass(), RuntimeException.class);
            assertEquals(ex.getMessage(), ErrorMessage.OPERATION_IS_NOT_AVAILABLE.text);
        }
    }

    @Test
    public void getTransfersFail2() throws Exception {
        Long id = -1L;
        try {
            transferService.getTransfers(id);
        } catch (Exception ex) {
            assertEquals(ex.getClass(), RuntimeException.class);
            assertEquals(ex.getMessage(), ErrorMessage.ACCOUNT_NOT_EXISTS.text);
        }
    }

    @Test
    public void getTransfersSuccess() throws Exception {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal transferAmount = BigDecimal.TEN;
        Transfer transfer = Transfer.builder().amount(transferAmount).description("Test").build();
        Account account = createAccount(amount);
        Optional<ErrorMessage> error = transferService.transfer(unlimitedAccount.getId(), account.getId(), transfer);
        assertEquals(error.isPresent(), false);
        assertEquals(transferService.getBalance(account.getId()), transferAmount);
    }

    @Test
    public void getTransfersByDate() throws Exception {
        BigDecimal amount = BigDecimal.ZERO;
        Account account = createAccount(amount);
        LocalDateTime startDate = LocalDateTime.now();
        transferService.transfer(unlimitedAccount.getId(), account.getId(), Transfer.builder().amount(BigDecimal.ONE).build());
        Thread.sleep(1000);
        LocalDateTime dateTime = LocalDateTime.now();
        Thread.sleep(1000);
        transferService.transfer(unlimitedAccount.getId(), account.getId(), Transfer.builder().amount(BigDecimal.TEN).build());
        List<TransferEvent> transfersByDate1 = transferService.getTransfersByDate(account.getId(), startDate, LocalDateTime.now());
        assertEquals(transfersByDate1.size(), 2);
        List<TransferEvent> transfersByDate2 = transferService.getTransfersByDate(account.getId(), dateTime, null);
        assertEquals(transfersByDate2.size(), 1);
        assertEquals(transfersByDate2.get(0).getAmount(), BigDecimal.TEN);
        List<TransferEvent> transfersByDate3 = transferService.getTransfersByDate(account.getId(), null, dateTime);
        assertEquals(transfersByDate3.size(), 1);
    }

    @Test
    public void transferFailInsufficientFunds() throws Exception {
        Account account = accountService.createAccount().get();
        Optional<ErrorMessage> errorMessage = testTransfer(account.getId(), unlimitedAccount.getId(), BigDecimal.TEN);
        assertEquals(errorMessage.get(), ErrorMessage.INSUFFICIENT_FUNDS);
    }

    @Test
    public void transferFailIncorrectAmount() throws Exception {
        Account account = accountService.createAccount().get();
        Optional<ErrorMessage> errorMessage = testTransfer(account.getId(), unlimitedAccount.getId(), BigDecimal.valueOf(-100));
        assertEquals(errorMessage.get(), ErrorMessage.NON_POSITIVE_AMOUNT);
    }

    private Optional<ErrorMessage> testTransfer(Long from, Long to, BigDecimal amount) throws Exception {
        Transfer transfer = Transfer.builder().amount(amount).description("Test").build();
        return transferService.transfer(from, to, transfer);
    }

}