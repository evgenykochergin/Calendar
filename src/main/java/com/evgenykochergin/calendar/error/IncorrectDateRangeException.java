package com.evgenykochergin.calendar.error;

import java.time.LocalDateTime;

import static java.lang.String.format;

public class IncorrectDateRangeException extends ApplicationException {
    public IncorrectDateRangeException(LocalDateTime fromDate, LocalDateTime toDate) {
        super(format("fromDate %s should be before toDate %s", fromDate, toDate));
    }
}
