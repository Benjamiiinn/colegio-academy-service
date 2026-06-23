package com.proyecto.academy_service.service;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.model.Matricula;
import com.proyecto.academy_service.repository.CursoRepository;
import com.proyecto.academy_service.repository.MatriculaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;
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

    private MatriculaService matriculaService;

    private Curso curso;
    private Matricula matricula;
    private Matricula matriculaActualizada;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        matriculaService = new MatriculaService(matriculaRepository, cursoRepository, webClient);
        ReflectionTestUtils.setField(matriculaService, "userPath", "/api/v1/usuarios/%d/exists");

        curso = Curso.builder()
                .id(1L)
                .nivel("1 Medio")
                .letra("A")
                .build();

        matricula = Matricula.builder()
                .id(1L)
                .estudianteId(10L)
                .curso(Curso.builder().id(1L).build())
                .build();

        matriculaActualizada = Matricula.builder()
                .id(1L)
                .estudianteId(20L)
                .curso(Curso.builder().id(2L).build())
                .build();

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));
    }

    @Test
    void matricularAlumno_cuandoEstudianteYCursoExisten_guardaYRetorna() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(false);
        when(matriculaRepository.save(any(Matricula.class))).thenReturn(matricula);

        Matricula resultado = matriculaService.matricularAlumno(matricula);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstudianteId()).isEqualTo(10L);
        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    void matricularAlumno_cuandoEstudianteNoExiste_lanzaResourceNotFoundException() {
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(false));

        assertThatThrownBy(() -> matriculaService.matricularAlumno(matricula))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no existe");

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void matricularAlumno_cuandoCursoNoExiste_lanzaRuntimeException() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.matricularAlumno(matricula))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Curso no existe");

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void matricularAlumno_cuandoYaMatriculado_lanzaBusinessRuleException() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> matriculaService.matricularAlumno(matricula))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya está matriculado");

        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void listarAlumnosPorCurso_retornaLista() {
        when(matriculaRepository.findByCursoId(1L)).thenReturn(List.of(matricula));

        List<Matricula> resultado = matriculaService.listarAlumnosPorCurso(1L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void actualizarMatricula_cuandoExisteYValida_actualizaYRetorna() {
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.save(any(Matricula.class))).thenReturn(matricula);

        Matricula resultado = matriculaService.actualizarMatricula(1L, matriculaActualizada);

        assertThat(resultado).isNotNull();
        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    void actualizarMatricula_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(matriculaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.actualizarMatricula(99L, matriculaActualizada))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarMatricula_cuandoExiste_elimina() {
        when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

        matriculaService.eliminarMatricula(1L);

        verify(matriculaRepository).delete(matricula);
    }

    @Test
    void eliminarMatricula_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(matriculaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.eliminarMatricula(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
