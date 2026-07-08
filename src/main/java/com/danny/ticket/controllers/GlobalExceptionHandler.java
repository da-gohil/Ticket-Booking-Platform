package com.danny.ticket.controllers;

import com.danny.ticket.domain.dtos.ErrorDTO;
import com.danny.ticket.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketSoldOutException.class)
    public ResponseEntity<ErrorDTO> handleTicketSoldOutException(TicketSoldOutException e){
        return clientError(e, HttpStatus.BAD_REQUEST, "Tickets are sold out for this ticket type");
    }

    @ExceptionHandler(QrCodeNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleQrCodeNotFoundException(QrCodeNotFoundException e){
        return clientError(e, HttpStatus.NOT_FOUND, "QR code not found");
    }

    @ExceptionHandler(QrCodeGenerationException.class)
    public ResponseEntity<ErrorDTO> handleQrCodeGenerationException(QrCodeGenerationException e){
        return serverError(e, "unable to generate the QR code");
    }

    @ExceptionHandler(EventUpdateException.class)
    public ResponseEntity<ErrorDTO> handleEventUpdateException(EventUpdateException e){
        return clientError(e, HttpStatus.BAD_REQUEST, "unable to update the event");
    }

    @ExceptionHandler(TicketTypeNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleTicketTypeUpdateException(TicketTypeNotFoundException e){
        return clientError(e, HttpStatus.NOT_FOUND, "Ticket type not found");
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleEventNotFoundException(EventNotFoundException e){
        return clientError(e, HttpStatus.NOT_FOUND, "Event not found");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUserNotFoundException(UserNotFoundException e){
        return clientError(e, HttpStatus.NOT_FOUND, "User not found");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ){
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                                .findFirst()
                                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                                .orElse("Validation error occurred");
        return clientError(e, HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolation(
            ConstraintViolationException e
    ){
        String errorMessage = e.getConstraintViolations()
                .stream()
                        .findFirst().map(violation -> violation.getPropertyPath() + ": " + violation.getMessage()
                ).orElse("Constraint violation occurred");
        return clientError(e, HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ){
        return clientError(e, HttpStatus.BAD_REQUEST,
                String.format("'%s' has an invalid value", e.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e){
        return serverError(e, "An unknown error occurred");
    }

    // Expected 4xx conditions: log at WARN without the full stack trace so real
    // server faults stay visible in the logs.
    private ResponseEntity<ErrorDTO> clientError(Exception e, HttpStatus status, String message){
        log.warn("{} -> {}: {}", e.getClass().getSimpleName(), status.value(), message);
        return body(status, message);
    }

    // Unexpected server faults: log at ERROR with the stack trace.
    private ResponseEntity<ErrorDTO> serverError(Exception e, String message){
        log.error("{} -> 500: {}", e.getClass().getSimpleName(), message, e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private ResponseEntity<ErrorDTO> body(HttpStatus status, String message){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError(message);
        return new ResponseEntity<>(errorDTO, status);
    }

}
