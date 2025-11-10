package com.moveo.ha.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InternalServerExceptionDTO {
    private String controller;
    private String controllerMethod;
    private String method;
    private String path;
    private String message;
    private String timestamp;
    private String exception; // simpleName
}
