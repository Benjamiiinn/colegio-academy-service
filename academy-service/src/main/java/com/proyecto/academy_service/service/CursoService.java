package com.proyecto.academy_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.repository.CursoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    @Transactional
    public Curso crearCurso(Curso curso) {
        boolean existe = cursoRepository.existsByNivelAndLetra(curso.getNivel(), curso.getLetra());
        if (existe) {
            throw new BusinessRuleException("Ya existe un curso registrado como '" + curso.getNivel() + " " + curso.getLetra() + "'.");
        }
        return cursoRepository.save(curso);
    }

    @Transactional(readOnly = true)
    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no existe"));
    }

    @Transactional
    public Curso actualizarCurso(Long id, Curso cursoActualizado) {
        Curso cursoExistente = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con ID: " + id));

        boolean existeOtro = cursoRepository.existsByNivelAndLetraAndIdNot(
                cursoActualizado.getNivel(),
                cursoActualizado.getLetra(),
                id
        );

        if (existeOtro) {
            throw new BusinessRuleException("Ya existe un curso registrado como '" + 
                    cursoActualizado.getNivel() + " " + cursoActualizado.getLetra() + "'.");
        }

        cursoExistente.setNivel(cursoActualizado.getNivel());
        cursoExistente.setLetra(cursoActualizado.getLetra());
        return cursoRepository.save(cursoExistente);
    }

    @Transactional
    public void eliminarCurso(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con ID: " + id));

        boolean tieneAsignaturas = !curso.getAsignaturas().isEmpty();
        boolean tieneMatriculas = !curso.getMatriculas().isEmpty();

        if (tieneAsignaturas || tieneMatriculas) {
            throw new BusinessRuleException("No se puede eliminar el curso porque tiene asignaturas o matriculas asociadas.");
        }

        cursoRepository.delete(curso);
    }
}
