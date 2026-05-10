package com.proyecto.academy_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Calificacion registrarNota(Calificacion calificacion) {
        Asignatura asignatura = asignaturaRepository.findById(calificacion.getAsignatura().getId())
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada"));

        Long cursoId = asignatura.getCurso().getId();
        boolean estaMatriculado = matriculaRepository.existsByEstudianteIdAndCursoId(
            calificacion.getEstudianteId(),
            cursoId
        );

        if (!estaMatriculado) {
            throw new RuntimeException("El alumno no pertenece al curso de esta asignatura.");
        }

        if (calificacion.getNota() < 1.0 || calificacion.getNota() > 7.0) {
            throw new RuntimeException("La nota debe estar entre 1.0 y 7.0.");
        }

        calificacion.setAsignatura(asignatura);
        return calificacionRepository.save(calificacion);
    }

    @Transactional(readOnly = true)
    public List<Calificacion> listarNotasPorEstudiante(Long estudianteId) {
        return calificacionRepository.findByEstudianteId(estudianteId);
    }
}
