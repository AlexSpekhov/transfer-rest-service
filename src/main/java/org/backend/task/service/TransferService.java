package org.backend.task.service;


import org.backend.task.dto.Transfer;
import org.backend.task.dto.ErrorMessage;
import org.backend.task.dto.event.TransferEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferService {

    BigDecimal getBalance(Long accountId) throws Exception;

    List<TransferEvent> getTransfers(Long accountId) throws Exception;

    List<TransferEvent> getTransfersByDate(Long accountId, LocalDateTime from, LocalDateTime to) throws Exception;

    Optional<ErrorMessage> transfer(Long debitAccountId, Long creditAccountId, Transfer transfer);
}
