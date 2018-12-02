package org.backend.task.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorMessage {
    ACCOUNT_NOT_EXISTS("Account doesn't exist"),
    INSUFFICIENT_FUNDS("Insufficient funds"),
    NON_POSITIVE_AMOUNT("Amount must be positive"),
    OPERATION_IS_NOT_AVAILABLE("Unavailable for this account"),
    SYSTEM_ERROR("System error");

    public final String text;

}
