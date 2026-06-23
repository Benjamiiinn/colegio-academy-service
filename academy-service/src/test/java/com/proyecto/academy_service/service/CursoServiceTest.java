package com.proyecto.academy_service.service;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.model.Matricula;
import com.proyecto.academy_service.repository.CursoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    private CursoService cursoService;

    private Curso curso;
    private Curso cursoActualizado;

    @BeforeEach
    void setUp() {
        cursoService = new CursoService(cursoRepository);

        curso = Curso.builder()
                .id(1L)
                .nivel("1 Medio")
                .letra("A")
                .asignaturas(Collections.emptyList())
                .matriculas(Collections.emptyList())
                .build();

        cursoActualizado = Curso.builder()
                .nivel("2 Medio")
                .letra("B")
                .build();
    }

    @Test
    void crearCurso_cuandoNoExisteDuplicado_guardaYRetornaCurso() {
        when(cursoRepository.existsByNivelAndLetra(curso.getNivel(), curso.getLetra())).thenReturn(false);
        when(cursoRepository.save(curso)).thenReturn(curso);

        Curso resultado = cursoService.crearCurso(curso);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNivel()).isEqualTo("1 Medio");
        assertThat(resultado.getLetra()).isEqualTo("A");
        verify(cursoRepository).save(curso);
    }

    @Test
    void crearCurso_cuandoYaExisteDuplicado_lanzaBusinessRuleException() {
        when(cursoRepository.existsByNivelAndLetra(curso.getNivel(), curso.getLetra())).thenReturn(true);

        assertThatThrownBy(() -> cursoService.crearCurso(curso))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya existe un curso");

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void listarCursos_retornaListaDeCursos() {
        when(cursoRepository.findAll()).thenReturn(List.of(curso));

        List<Curso> resultado = cursoService.listarCursos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNivel()).isEqualTo("1 Medio");
    }

    @Test
    void buscarPorId_cuandoExiste_retornaCurso() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        Curso resultado = cursoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Curso no existe");
    }

    @Test
    void actualizarCurso_cuandoExisteYNoHayDuplicado_actualizaYRetorna() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByNivelAndLetraAndIdNot(
                cursoActualizado.getNivel(), cursoActualizado.getLetra(), 1L)).thenReturn(false);
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        Curso resultado = cursoService.actualizarCurso(1L, cursoActualizado);

        assertThat(resultado.getNivel()).isEqualTo("2 Medio");
        assertThat(resultado.getLetra()).isEqualTo("B");
        verify(cursoRepository).save(any(Curso.class));
    }

    @Test
    void actualizarCurso_cuandoExisteDuplicado_lanzaBusinessRuleException() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.existsByNivelAndLetraAndIdNot(
                cursoActualizado.getNivel(), cursoActualizado.getLetra(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> cursoService.actualizarCurso(1L, cursoActualizado))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya existe un curso");

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void actualizarCurso_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.actualizarCurso(99L, cursoActualizado))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarCurso_cuandoNoTieneAsignaturasNiMatriculas_elimina() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        cursoService.eliminarCurso(1L);

        verify(cursoRepository).delete(curso);
    }

    @Test
    void eliminarCurso_cuandoTieneAsignaturas_lanzaBusinessRuleException() {
        curso.setAsignaturas(List.of(new Asignatura()));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> cursoService.eliminarCurso(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede eliminar");

        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void eliminarCurso_cuandoTieneMatriculas_lanzaBusinessRuleException() {
        curso.setMatriculas(List.of(new Matricula()));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> cursoService.eliminarCurso(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede eliminar");

        verify(cursoRepository, never()).delete(any());
    }

    @Test
    void eliminarCurso_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.eliminarCurso(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
