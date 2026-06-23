package com.proyecto.academy_service.service;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Calificacion;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.repository.AsignaturaRepository;
import com.proyecto.academy_service.repository.CalificacionRepository;
import com.proyecto.academy_service.repository.MatriculaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private AsignaturaRepository asignaturaRepository;
    @Mock
    private CalificacionRepository calificacionRepository;

    private CalificacionService calificacionService;

    private Curso curso;
    private Asignatura asignatura;
    private Calificacion calificacion;
    private Calificacion calificacionActualizada;
    private final Long docenteId = 100L;
    private final Long otroDocenteId = 999L;

    @BeforeEach
    void setUp() {
        calificacionService = new CalificacionService(matriculaRepository, asignaturaRepository, calificacionRepository);

        curso = Curso.builder().id(1L).nivel("1 Medio").letra("A").build();

        asignatura = Asignatura.builder()
                .id(1L)
                .nombre("Matematicas")
                .curso(curso)
                .build();

        calificacion = Calificacion.builder()
                .id(1L)
                .estudianteId(10L)
                .asignatura(Asignatura.builder().id(1L).curso(curso).build())
                .nota(5.5)
                .descripcion("Prueba parcial")
                .docenteId(docenteId)
                .build();

        calificacionActualizada = Calificacion.builder()
                .estudianteId(10L)
                .nota(6.0)
                .descripcion("Nota corregida")
                .build();
    }

    @Test
    void registrarNota_cuandoAlumnoMatriculadoYNotaValida_guardaYRetorna() {
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(true);
        when(calificacionRepository.save(any(Calificacion.class))).thenReturn(calificacion);

        Calificacion resultado = calificacionService.registrarNota(calificacion, docenteId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNota()).isEqualTo(5.5);
        verify(calificacionRepository).save(any(Calificacion.class));
    }

    @Test
    void registrarNota_cuandoAsignaturaNoExiste_lanzaResourceNotFoundException() {
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calificacionService.registrarNota(calificacion, docenteId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Asignatura no encontrada");

        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void registrarNota_cuandoAlumnoNoMatriculado_lanzaBusinessRuleException() {
        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> calificacionService.registrarNota(calificacion, docenteId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no pertenece al curso");

        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void registrarNota_cuandoNotaMenorA1_lanzaBusinessRuleException() {
        calificacion.setNota(0.5);

        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> calificacionService.registrarNota(calificacion, docenteId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nota debe estar entre");

        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void registrarNota_cuandoNotaMayorA7_lanzaBusinessRuleException() {
        calificacion.setNota(7.5);

        when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(asignatura));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> calificacionService.registrarNota(calificacion, docenteId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nota debe estar entre");

        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void listarNotasPorEstudiante_retornaLista() {
        when(calificacionRepository.findByEstudianteId(10L)).thenReturn(List.of(calificacion));

        List<Calificacion> resultado = calificacionService.listarNotasPorEstudiante(10L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void actualizarCalificacion_cuandoMismoDocenteYValida_actualizaYRetorna() {
        when(calificacionRepository.findById(1L)).thenReturn(Optional.of(calificacion));
        when(matriculaRepository.existsByEstudianteIdAndCursoId(10L, 1L)).thenReturn(true);
        when(calificacionRepository.save(any(Calificacion.class))).thenReturn(calificacion);

        Calificacion resultado = calificacionService.actualizarCalificacion(1L, calificacionActualizada, docenteId);

        assertThat(resultado).isNotNull();
        verify(calificacionRepository).save(any(Calificacion.class));
    }

    @Test
    void actualizarCalificacion_cuandoDocenteDiferente_lanzaBusinessRuleException() {
        when(calificacionRepository.findById(1L)).thenReturn(Optional.of(calificacion));

        assertThatThrownBy(() -> calificacionService.actualizarCalificacion(1L, calificacionActualizada, otroDocenteId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso");

        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void actualizarCalificacion_cuandoNotaInvalida_lanzaBusinessRuleException() {
        calificacionActualizada.setNota(8.0);

        when(calificacionRepository.findById(1L)).thenReturn(Optional.of(calificacion));

        assertThatThrownBy(() -> calificacionService.actualizarCalificacion(1L, calificacionActualizada, docenteId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nota debe estar entre");

        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void eliminarCalificacion_cuandoMismoDocente_elimina() {
        when(calificacionRepository.findById(1L)).thenReturn(Optional.of(calificacion));

        calificacionService.eliminarCalificacion(1L, docenteId);

        verify(calificacionRepository).delete(calificacion);
    }

    @Test
    void eliminarCalificacion_cuandoDocenteDiferente_lanzaBusinessRuleException() {
        when(calificacionRepository.findById(1L)).thenReturn(Optional.of(calificacion));

        assertThatThrownBy(() -> calificacionService.eliminarCalificacion(1L, otroDocenteId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No tienes permiso");

        verify(calificacionRepository, never()).delete(any());
    }
}
