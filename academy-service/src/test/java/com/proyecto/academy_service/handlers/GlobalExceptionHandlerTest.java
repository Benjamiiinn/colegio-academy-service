package com.proyecto.academy_service.handlers;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ErrorResponse;
import com.proyecto.academy_service.exception.GlobalExceptionHandler;
import com.proyecto.academy_service.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;
    @Mock
    private MethodArgumentNotValidException validationException;
    @Mock
    private BindingResult bindingResult;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("/api/v1/test");
    }

    @Test
    void handleResourceNotFoundException_retorna404() {
        var ex = new ResourceNotFoundException("Curso no encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Curso no encontrado");
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleBusinessRuleException_retorna400() {
        var ex = new BusinessRuleException("Regla de negocio violada");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRuleException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Regla de negocio violada");
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void handleValidationExceptions_retorna400() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("obj", "nivel", "El nivel es obligatorio"),
                        new FieldError("obj", "letra", "La letra es obligatoria")));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(validationException, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("nivel: El nivel es obligatorio");
        assertThat(response.getBody().getMessage()).contains("letra: La letra es obligatoria");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void handleGenericException_retorna500() {
        var ex = new RuntimeException("Error inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Error interno del servidor");
        assertThat(response.getBody().getMessage()).contains("Error inesperado");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }
}
