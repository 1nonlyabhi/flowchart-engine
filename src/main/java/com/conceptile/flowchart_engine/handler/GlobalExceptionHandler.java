package com.conceptile.flowchart_engine.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put(
        "timestamp",
        ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    body.put("status", ex.getStatusCode().value());
    body.put("error", ex.getReason());
    body.put("path", request.getRequestURI());

    return new ResponseEntity<>(body, ex.getStatusCode());
  }
}
