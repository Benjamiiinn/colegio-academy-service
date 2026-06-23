package com.proyecto.academy_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.repository.AsignaturaRepository;
import com.proyecto.academy_service.repository.CursoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final CursoRepository cursoRepository;
    private final WebClient webClient;

    @Value("${api.user.exists}")
    private String userPath;

    @Transactional
    public Asignatura crearAsignatura(Asignatura asignatura) {
        Boolean existeDocente = webClient.get()
        .uri(String.format(userPath, asignatura.getDocenteId()))
        .retrieve()
        .bodyToMono(Boolean.class)
        .block();
        if (Boolean.FALSE.equals(existeDocente)) {
            throw new ResourceNotFoundException("El docente con ID " + asignatura.getDocenteId() + " no existe.");
        }

        Curso curso = cursoRepository.findById(asignatura.getCurso().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        asignatura.setCurso(curso);
        return asignaturaRepository.save(asignatura);
    }

    @Transactional(readOnly = true)
    public List<Asignatura> listarPorDocente(Long docenteId) {
        return asignaturaRepository.findByDocenteId(docenteId);
    }

    @Transactional(readOnly = true)
    public List<Asignatura> listarPorCurso(Long cursoId) {
        return asignaturaRepository.findByCursoId(cursoId);
    }

    @Transactional(readOnly = true)
    public List<Asignatura> listarTodas() {
        return asignaturaRepository.findAll();
    }

    @Transactional
    public Asignatura actualizarAsignatura(Long id, Asignatura asignaturaActualizada) {
        Asignatura asignaturaExistente = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con ID: " + id));

        Boolean existeDocente = webClient.get()
                .uri(String.format(userPath, asignaturaActualizada.getDocenteId()))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (Boolean.FALSE.equals(existeDocente)) {
            throw new ResourceNotFoundException("El docente con ID " + asignaturaActualizada.getDocenteId() + " no existe.");
        }

        Curso curso = cursoRepository.findById(asignaturaActualizada.getCurso().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con ID: " + asignaturaActualizada.getCurso().getId()));

        asignaturaExistente.setNombre(asignaturaActualizada.getNombre());
        asignaturaExistente.setDocenteId(asignaturaActualizada.getDocenteId());
        asignaturaExistente.setCurso(curso);

        return asignaturaRepository.save(asignaturaExistente);
    }

    @Transactional
    public void eliminarAsignatura(Long id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con ID: " + id));

        boolean tieneCalificaciones = !asignatura.getCalificaciones().isEmpty();

        if (tieneCalificaciones) {
            throw new BusinessRuleException("No se puede eliminar la asignatura porque tiene calificaciones asociadas.");
        }

        asignaturaRepository.delete(asignatura);
    }
}
