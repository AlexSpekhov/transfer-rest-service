package org.backend.task.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Transfer {
    private final BigDecimal amount;
    private final String description;

    @JsonCreator
    public Transfer(@JsonProperty("amount") BigDecimal amount,
                    @JsonProperty("description") String description) {
        this.amount = amount;
        this.description = description;
    }
}
