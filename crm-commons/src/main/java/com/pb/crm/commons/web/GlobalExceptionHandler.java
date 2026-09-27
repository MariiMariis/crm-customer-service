package com.pb.crm.commons.web;

import com.pb.crm.commons.error.BusinessRuleException;
import com.pb.crm.commons.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex) {
        String message = ex instanceof ResourceNotFoundException ? ex.getMessage() : "Recurso nao encontrado";
        return respond(HttpStatus.NOT_FOUND, "Not Found", message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Um ou mais campos sao invalidos",
                details
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        String message = ex instanceof IllegalArgumentException ? ex.getMessage() : "Parametro ou corpo da requisicao invalido";
        return respond(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "Metodo HTTP nao suportado para este recurso");
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex) {
        return respond(HttpStatus.CONFLICT, "Business Rule Violation", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        return respond(
                HttpStatus.CONFLICT,
                "Data Integrity Violation",
                "A operacao viola restricoes de integridade do banco de dados (registro duplicado ou com vinculos dependentes)"
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return respond(
                HttpStatus.CONFLICT,
                "Concurrent Modification",
                "O registro foi alterado por outra operacao. Recarregue os dados e tente novamente"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Erro inesperado ao processar a requisicao", ex);
        return respond(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Ocorreu um erro inesperado. Tente novamente mais tarde."
        );
    }

    private static ResponseEntity<ApiError> respond(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), error, message));
    }
}
