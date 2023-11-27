package com.example.orderservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

import static com.example.orderservice.constants.OrderConstants.ERROR_MESSAGE;
import static com.example.orderservice.constants.OrderConstants.STATUS;


@ControllerAdvice
public class GlobalExceptionHandler {
    private final HashMap<String, String> map = new HashMap<>();
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<Object> customException(CustomException cx) {
        map.clear();

        map.put(ERROR_MESSAGE, cx.getMessage());
        map.put(STATUS, String.valueOf(cx.getStatus()));
        logger.error("Error encountered {} with message {}", cx.getStatus(), cx.getMessage());
        return new ResponseEntity<>(map, cx.getStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> invalidArgsException(MethodArgumentNotValidException mx) {
        map.clear();

        mx.getBindingResult().getFieldErrors().forEach(fieldError ->
                map.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        logger.error("Invalid argument(s) exception encountered - {}", mx.getMessage());
//        mx.printStackTrace();
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> illegalArgsException(IllegalArgumentException ix) {
        map.clear();

        map.put(ERROR_MESSAGE, ix.getMessage());
        logger.error("Illegal argument(s) exception encountered - {}", ix.getLocalizedMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

/*    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> accessDeniedException(AccessDeniedException ax) {
        map.clear();

        map.put(ERROR_MESSAGE, ax.getMessage());
        logger.error("access denied exception occurred");
        return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
    }*/

    @ExceptionHandler(value = MissingRequestHeaderException.class)
    public ResponseEntity<Object> missingHeader(MissingRequestHeaderException mx) {
        map.clear();

        map.put(ERROR_MESSAGE, mx.getMessage());
        logger.error("Missing header");
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<Object> sqlError(DataIntegrityViolationException dx) {
        map.clear();

        map.put(ERROR_MESSAGE, "Database error");
        logger.error(dx.getMessage());
        return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> otherFailures(Exception ex) {
        map.clear();
/*

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
*/

        map.put(ERROR_MESSAGE, ex.getMessage());
        map.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.toString());

        return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
