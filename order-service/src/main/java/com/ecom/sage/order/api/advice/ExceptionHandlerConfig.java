package com.ecom.sage.order.api.advice;

import com.ecom.sage.order.api.exception.OrderIdNotFoundException;
import com.google.common.flogger.FluentLogger;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerConfig {
    private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<String> handleBulkheadException(BulkheadFullException ex) {
        LOGGER.atInfo().log("⛔ Too many requests. Try again later.");
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("⛔ Too many requests. Try again later.");
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<?> handleRateLimiterException(RequestNotPermitted ex) {
        LOGGER.atInfo().log("⚠ Rate limit exceeded. Try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("⚠ Rate limit exceeded. Try again later.");
    }

    @ExceptionHandler(OrderIdNotFoundException.class)
    public ResponseEntity<?> orderIdNotFoundException(OrderIdNotFoundException ex) {
        LOGGER.atInfo().log("⚠ OrderId not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("⚠" + ex.getMessage());
    }


}
