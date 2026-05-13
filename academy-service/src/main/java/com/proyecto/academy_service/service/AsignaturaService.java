package com.proyecto.academy_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.proyecto.academy_service.exception.ResourceNotFoundException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.repository.AsignaturaRepository;
import com.proyecto.academy_service.repository.CursoRepository;

@Service
public class AsignaturaService {

    @Autowired
    private AsignaturaRepository asignaturaRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private WebClient webClient;
    

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
}
