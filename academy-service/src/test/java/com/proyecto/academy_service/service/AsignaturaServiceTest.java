package com.proyecto.academy_service.service;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.repository.AsignaturaRepository;
import com.proyecto.academy_service.repository.CursoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignaturaServiceTest {

    @Mock
    private AsignaturaRepository asignaturaRepository;
    @Mock
    private CursoRepository cursoRepository;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AsignaturaService asignaturaService;

    private Curso curso;
    private Asignatura asignatura;
    private Asignatura asignaturaActualizada;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        asignaturaService = new AsignaturaService(asignaturaRepository, cursoRepository, webClient);
        ReflectionTestUtils.setField(asignaturaService, "userPath", "/api/v1/usuarios/%d/exists");

        curso = Curso.builder().id(1L).nivel("1 Medio").letra("A").build();

        asignatura = Asignatura.builder()
                .id(1L)
                .nombre("Matematicas")
                .docenteId(100L)
                .curso(Curso.builder().id(1L).build())
                .calificaciones(Collections.emptyList())
                .build();

        asignaturaActualizada = Asignatura.builder()
                .nombre("Lenguaje")
                .docenteId(200L)
                .curso(Curso.builder().id(2L).build())
                .build();

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));
    }

    @Test
    void crearAsignatura_cuandoDocenteYCursoExisten_guardaYRetorna() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(asignaturaRepository.save(any(Asignatura.class))).thenReturn(asignatura);

        Asignatura resultado = asignaturaService.crearAsignatura(asignatura);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Matematicas");
        verify(asignaturaRepository).save(any(Asignatura.class));
    }

    @Test
    void crearAsignatura_cuandoDocenteNoExiste_lanzaResourceNotFoundException() {
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(false));

        assertThatThrownBy(() -> asignaturaService.crearAsignatura(asignatura))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("docente");

        verify(asignaturaRepository, never()).save(any());
    }

    @Test
    void crearAsignatura_cuandoCursoNoExiste_lanzaResourceNotFoundException() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignaturaService.crearAsignatura(asignatura))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Curso no encontrado");

        verify(asignaturaRepository, never()).save(any());
    }

    @Test
    void listarPorDocente_retornaLista() {
        when(asignaturaRepository.findByDocenteId(100L)).thenReturn(List.of(asignatura));

        List<Asignatura> resultado = asignaturaService.listarPorDocente(100L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDocenteId()).isEqualTo(100L);
    }

    @Test
    void listarTodas_retornaLista() {
        when(asignaturaRepository.findAll()).thenReturn(List.of(asignatura));

        List<Asignatura> resultado = asignaturaService.listarTodas();

        assertThat(resultado).hasSize(1);
    }

    @Test
    void actualizarAsignatura_cuandoExisteYValida_actualizaYRetorna() {
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso));
        when(asignaturaRepository.save(any(Asignatura.class))).thenReturn(asignatura);

        Asignatura resultado = asignaturaService.actualizarAsignatura(1L, asignaturaActualizada);

        assertThat(resultado).isNotNull();
        verify(asignaturaRepository).save(any(Asignatura.class));
    }

    @Test
    void actualizarAsignatura_cuandoDocenteNoExiste_lanzaResourceNotFoundException() {
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(false));

        assertThatThrownBy(() -> asignaturaService.actualizarAsignatura(1L, asignaturaActualizada))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("docente");

        verify(asignaturaRepository, never()).save(any());
    }

    @Test
    void eliminarAsignatura_cuandoNoTieneCalificaciones_elimina() {
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));

        asignaturaService.eliminarAsignatura(1L);

        verify(asignaturaRepository).delete(asignatura);
    }

    @Test
    void eliminarAsignatura_cuandoTieneCalificaciones_lanzaBusinessRuleException() {
        asignatura.setCalificaciones(List.of(new com.proyecto.academy_service.model.Calificacion()));
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));

        assertThatThrownBy(() -> asignaturaService.eliminarAsignatura(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede eliminar");

        verify(asignaturaRepository, never()).delete(any());
    }

    @Test
    void eliminarAsignatura_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(asignaturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignaturaService.eliminarAsignatura(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
