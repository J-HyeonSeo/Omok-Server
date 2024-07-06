package com.jhsfully.omokserver.exception;

import com.jhsfully.omokserver.dto.ErrorResponse;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<?> inputArgsExceptionHandler(BindingResult bindingResult) {
        String message = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();

        if (!StringUtils.hasText(message)) {
            message = "요청된 값이 올바르지 않습니다.";
        }

        return ResponseEntity.badRequest()
            .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }

}
