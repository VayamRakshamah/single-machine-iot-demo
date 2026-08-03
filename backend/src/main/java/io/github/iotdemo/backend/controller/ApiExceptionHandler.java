package io.github.iotdemo.backend.controller;
import org.springframework.http.*;import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.bind.annotation.*;
import java.time.Instant;import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler({NoSuchElementException.class})@ResponseStatus(HttpStatus.NOT_FOUND)Map<String,Object> notFound(Exception e){return error("Not found");}
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class,MethodArgumentNotValidException.class})@ResponseStatus(HttpStatus.BAD_REQUEST)Map<String,Object> badRequest(Exception e){return error(e.getMessage());}
 private Map<String,Object> error(String message){return Map.of("timestamp",Instant.now(),"message",message);}
}

