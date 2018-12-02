package org.backend.task.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Builder
public class Account {
    private final Long id;
    @Setter
    private AccountState state;
    @Setter
    private BigDecimal balance;


    @JsonCreator
    public Account(@JsonProperty("id") Long id,
                   @JsonProperty("state") AccountState state,
                   @JsonProperty("balance") BigDecimal balance) {
        this.id = id;
        this.state = state;
        this.balance = balance;
    }
}
