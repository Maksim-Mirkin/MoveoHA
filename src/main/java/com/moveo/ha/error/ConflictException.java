package com.moveo.ha.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends MoveoHAException {
    public ConflictException(String message) { super(message); }
}

