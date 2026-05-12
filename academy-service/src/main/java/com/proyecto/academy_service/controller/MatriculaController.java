package com.proyecto.academy_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.academy_service.dto.MatriculaDTO;
import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.model.Matricula;
import com.proyecto.academy_service.service.MatriculaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @PostMapping
    public ResponseEntity<MatriculaDTO> matricular(
            @RequestHeader(value = "X-User-Role", required = false) String rolUsuario,
            @Valid @RequestBody MatriculaDTO dto) {
                
        if (rolUsuario == null || (!rolUsuario.contains("ROLE_ADMIN"))) {
            throw new BusinessRuleException("Acceso denegado: Solo los administradores pueden matricular alumnos.");
        }

        Matricula matricula = matriculaService.matricularAlumno(dto.toModel());
        return ResponseEntity.ok(MatriculaDTO.fromModel(matricula));
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<MatriculaDTO>> listarAlumnosPorCurso(@PathVariable Long cursoId) {
        List<MatriculaDTO> lista = matriculaService.listarAlumnosPorCurso(cursoId).stream()
                .map(MatriculaDTO::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}
