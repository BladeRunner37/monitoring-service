package org.example.monitoring.rest.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.example.monitoring.exception.BusinessException;
import org.example.monitoring.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<Error> handleBusinessException(BusinessException e) {
        log.error("Business error", e);
        Error error = new Error();
        error.setErrorMessage(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(error);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Request validation exception", e);
        Error error = new Error();
        error.setErrorMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Error> handleException(Exception e) {
        log.error("Technical error", e);
        Error error = new Error();
        error.setErrorMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
