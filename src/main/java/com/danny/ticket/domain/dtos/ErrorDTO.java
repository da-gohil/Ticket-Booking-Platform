package com.danny.ticket.domain.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
public class ErrorDTO {
    private String error;
}
