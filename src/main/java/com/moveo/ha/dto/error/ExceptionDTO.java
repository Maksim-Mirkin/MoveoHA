package com.moveo.ha.dto.error;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ExceptionDTO {
    private String controller;
    private String controllerMethod;
    private String method;
    private String path;
    private String message;
    private int status;
    private String timestamp;

    public Map<String, Object> dtoToMap() {
        return Map.of(
                "controller", controller,
                "controllerMethod", controllerMethod,
                "method", method,
                "path", path,
                "message", message,
                "status", status,
                "timestamp", timestamp
        );
    }
}
