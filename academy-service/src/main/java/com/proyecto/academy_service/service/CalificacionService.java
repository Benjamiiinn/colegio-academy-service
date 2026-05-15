package com.proyecto.academy_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Calificacion;
import com.proyecto.academy_service.repository.AsignaturaRepository;
import com.proyecto.academy_service.repository.CalificacionRepository;
import com.proyecto.academy_service.repository.MatriculaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final MatriculaRepository matriculaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final CalificacionRepository calificacionRepository;

    @Transactional
    public Calificacion registrarNota(Calificacion calificacion, Long docenteId) {
        Asignatura asignatura = asignaturaRepository.findById(calificacion.getAsignatura().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada"));

        Long cursoId = asignatura.getCurso().getId();
        boolean estaMatriculado = matriculaRepository.existsByEstudianteIdAndCursoId(
            calificacion.getEstudianteId(),
            cursoId
        );

        if (!estaMatriculado) {
            throw new BusinessRuleException("El alumno no pertenece al curso de esta asignatura.");
        }

        if (calificacion.getNota() < 1.0 || calificacion.getNota() > 7.0) {
            throw new BusinessRuleException("La nota debe estar entre 1.0 y 7.0.");
        }

        calificacion.setAsignatura(asignatura);
        calificacion.setDocenteId(docenteId);
        return calificacionRepository.save(calificacion);
    }

    @Transactional(readOnly = true)
    public List<Calificacion> listarNotasPorEstudiante(Long estudianteId) {
        return calificacionRepository.findByEstudianteId(estudianteId);
    }

    @Transactional(readOnly = true)
    public List<Calificacion> listarTodas() {
        return calificacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Calificacion> listarPorDocente(Long docenteId) {
        return calificacionRepository.findByDocenteId(docenteId);
    }

    @Transactional
    public Calificacion actualizarCalificacion(Long id, Calificacion calificacionActualizada, Long docenteIdAutenticado) {
        Calificacion calificacionExistente = calificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calificacion no encontrada con ID: " + id));

        Long docenteCalificacion = calificacionExistente.getDocenteId();

        if (!docenteCalificacion.equals(docenteIdAutenticado)) {
            throw new BusinessRuleException("No tienes permiso para modificar esta calificacion. Solo el docente que la creo puede editarla.");
        }

        Asignatura asignatura = calificacionExistente.getAsignatura();

        if (calificacionActualizada.getNota() < 1.0 || calificacionActualizada.getNota() > 7.0) {
            throw new BusinessRuleException("La nota debe estar entre 1.0 y 7.0.");
        }

        boolean estaMatriculado = matriculaRepository.existsByEstudianteIdAndCursoId(
                calificacionActualizada.getEstudianteId(),
                asignatura.getCurso().getId()
        );

        if (!estaMatriculado) {
            throw new BusinessRuleException("El alumno no pertenece al curso de esta asignatura.");
        }

        calificacionExistente.setNota(calificacionActualizada.getNota());
        calificacionExistente.setDescripcion(calificacionActualizada.getDescripcion());
        calificacionExistente.setEstudianteId(calificacionActualizada.getEstudianteId());

        return calificacionRepository.save(calificacionExistente);
    }

    @Transactional
    public void eliminarCalificacion(Long id, Long docenteIdAutenticado) {
        Calificacion calificacion = calificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calificacion no encontrada con ID: " + id));

        Long docenteCalificacion = calificacion.getDocenteId();

        if (!docenteCalificacion.equals(docenteIdAutenticado)) {
            throw new BusinessRuleException("No tienes permiso para eliminar esta calificacion. Solo el docente que la creo puede eliminarla.");
        }

        calificacionRepository.delete(calificacion);
    }
}
