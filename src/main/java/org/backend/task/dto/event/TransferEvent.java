package org.backend.task.dto.event;

import lombok.Builder;
import lombok.Getter;
import org.backend.task.dto.TransferDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransferEvent {
    private final TransferDirection direction;
    private final BigDecimal amount;
    private final String description;
    private final Long accountId;
    private final LocalDateTime dateTime;
}
