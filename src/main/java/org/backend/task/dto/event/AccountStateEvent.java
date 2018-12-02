package org.backend.task.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.backend.task.dto.AccountState;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AccountStateEvent {
    private final Long accountId;
    private final AccountState state;
    private final LocalDateTime dateTime;

    public AccountStateEvent(Long accountId, AccountState state) {
        this.accountId = accountId;
        this.state = state;
        this.dateTime = LocalDateTime.now();
    }
}
