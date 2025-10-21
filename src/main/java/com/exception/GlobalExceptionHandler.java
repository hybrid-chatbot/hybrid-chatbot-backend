package com.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        // 에러 로그를 먼저 기록합니다.
        log.error("An unexpected error occurred: ", e);

        // 프론트엔드에 보낼 에러 메시지를 만듭니다.
        Map<String, String> errorResponse = Map.of(
            "error", "An unexpected error occurred",
            "message", e.getMessage()
        );

        // HTTP 500 (Internal Server Error) 상태 코드와 함께 응답을 보냅니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}