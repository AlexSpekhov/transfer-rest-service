package org.backend.task.handler;

import org.apache.commons.lang3.StringUtils;
import org.backend.task.dto.Transfer;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

public class Validator {

    public static String FORMAT = "yyyy-MM-dd-HH:mm:ss";

    boolean isInvalidId(String id) {
        return StringUtils.isEmpty(id) || !StringUtils.isNumeric(id);
    }

    boolean isInvalidTransfer(Transfer transfer) {
        return Objects.isNull(transfer) ||
                Objects.isNull(transfer.getAmount()) ||
                transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0;
    }

    Optional<LocalDateTime> getLocalDateTime(String sDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT);
        try {
            if (StringUtils.isEmpty(sDate)) {
                return Optional.empty();
            }
            return Optional.ofNullable(LocalDateTime.parse(sDate, formatter));
        } catch (DateTimeParseException ex) {
            throw new DateTimeException("Incorrect format date. Available format:" + FORMAT);
        }
    }
}
