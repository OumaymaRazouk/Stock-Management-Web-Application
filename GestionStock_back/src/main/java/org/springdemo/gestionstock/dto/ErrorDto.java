package org.springdemo.gestionstock.dto;

import lombok.Data;

@Data
public class ErrorDto {
    private String message;

    public ErrorDto(String message) {
        this.message = message;
    }
}