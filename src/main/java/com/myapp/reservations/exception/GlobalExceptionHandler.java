package com.myapp.reservations.exception;

import com.myapp.reservations.exception.notfoundexceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler  {

    @ExceptionHandler(IllegalAccessError.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex ,
            HttpServletRequest request){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        404,
                        ex.getMessage(),
                        request.getRequestURI()
        ));

    }
/*
    @ExceptionHandler()
    public ResponseEntity<ErrorResponse> handle(
            RuntimeException ex ,
            HttpServletRequest request){

        return ResponseEntity.status(HttpStatus.)
                .body(new ErrorResponse(
                        ,
                        ex.getMessage(),
                        request.getRequestURI()
                ));

    }
*/
}
