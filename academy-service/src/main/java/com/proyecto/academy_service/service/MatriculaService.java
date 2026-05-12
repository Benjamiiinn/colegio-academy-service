package com.proyecto.academy_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.model.Matricula;
import com.proyecto.academy_service.repository.CursoRepository;
import com.proyecto.academy_service.repository.MatriculaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final CursoRepository cursoRepository;
    private final WebClient webClient;

    @Value("${api.user.exists}")
    private String userPath;

    @Transactional
    public Matricula matricularAlumno(Matricula matricula) {
        Boolean existeEstudiante = webClient.get()
            .uri(String.format(userPath, matricula.getEstudianteId()))
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();
        
        if (Boolean.FALSE.equals(existeEstudiante)) {
            throw new ResourceNotFoundException("El estudiante ID " + matricula.getEstudianteId() + " no existe en el sistema.");
        }

        Curso curso = cursoRepository.findById(matricula.getCurso().getId())
                .orElseThrow(() -> new RuntimeException("Curso no existe"));

        boolean yaMatriculado = matriculaRepository.existsByEstudianteIdAndCursoId(matricula.getEstudianteId(), curso.getId());
        if (yaMatriculado) {
            throw new BusinessRuleException("El estudiante ya está matriculado en este curso.");
        }

        matricula.setCurso(curso);
        return matriculaRepository.save(matricula);
    }

    @Transactional(readOnly = true)
    public List<Matricula> listarAlumnosPorCurso(Long cursoId) {
        return matriculaRepository.findByCursoId(cursoId);
    }
}
