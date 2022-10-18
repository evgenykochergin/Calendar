package com.evgenykochergin.calendar.error;

import com.evgenykochergin.calendar.Application;

public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }
}
