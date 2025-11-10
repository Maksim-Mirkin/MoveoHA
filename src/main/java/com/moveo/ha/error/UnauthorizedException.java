package com.moveo.ha.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends MoveoHAException {
    public UnauthorizedException(String message) { super(message); }
}

