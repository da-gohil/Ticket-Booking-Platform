package com.danny.ticket.controllers;

import com.danny.ticket.domain.dtos.ErrorDTO;
import com.danny.ticket.exceptions.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUserNotFoundException(UserNotFoundException e){
        log.error("Caught UserNotFoundException", e);
        ErrorDTO errorDTO = new ErrorDTO();

        errorDTO.setError("User not found");
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ){
        log.error("Caught MethodArgumentNotValidException", e);
        ErrorDTO errorDTO = new ErrorDTO();

        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                                .findFirst()
                                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                                .orElse("Validation error occurred");
        errorDTO.setError(errorMessage);
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolation(
            ConstraintViolationException e
    ){
        log.error("Caught ConstraintViolationException", e);
        ErrorDTO errorDTO = new ErrorDTO();
        String errorMessage = e.getConstraintViolations()
                .stream()
                        .findFirst().map(violation -> violation.getPropertyPath() + ": " + violation.getMessage()
                ).orElse("Constraint violation occurred");

        errorDTO.setError(errorMessage);
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e){
        log.error("Caught Exception", e);
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("An unknown error occurred");
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
