package br.edu.ufvjm.mestre.identity.adapter.http;

import br.edu.ufvjm.mestre.identity.application.EmailAlreadyRegistered;
import br.edu.ufvjm.mestre.identity.domain.InvalidRegistration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RegistrationController.class)
public class RegistrationErrors {
    public record ErrorResponse(String code, String message) {}

    @ExceptionHandler(InvalidRegistration.class)
    ResponseEntity<ErrorResponse> invalid(InvalidRegistration error) {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REGISTRATION", error.getMessage()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> malformed() {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", "Envie um JSON válido com name, email e password."));
    }
    @ExceptionHandler(EmailAlreadyRegistered.class)
    ResponseEntity<ErrorResponse> duplicate(EmailAlreadyRegistered error) {
        return ResponseEntity.status(409).body(new ErrorResponse("REGISTRATION_CONFLICT", error.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected() {
        return ResponseEntity.internalServerError().body(new ErrorResponse("INTERNAL_ERROR", "Não foi possível concluir o cadastro. Tente novamente mais tarde."));
    }
}
