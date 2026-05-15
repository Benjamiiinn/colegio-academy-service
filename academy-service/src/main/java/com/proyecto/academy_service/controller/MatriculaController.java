package com.proyecto.academy_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<MatriculaDTO> matricular(@Valid @RequestBody MatriculaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (roles == null || (!roles.contains("ROLE_ADMIN"))) {
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

    @GetMapping
    public ResponseEntity<List<MatriculaDTO>> listarTodas() {
        List<MatriculaDTO> lista = matriculaService.listarTodas().stream()
                .map(MatriculaDTO::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatriculaDTO> actualizarMatricula(@PathVariable Long id, @Valid @RequestBody MatriculaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (roles == null || (!roles.contains("ROLE_ADMIN"))) {
            throw new BusinessRuleException("Acceso denegado: Solo los administradores pueden modificar matriculas.");
        }

        Matricula matricula = matriculaService.actualizarMatricula(id, dto.toModel());
        return ResponseEntity.ok(MatriculaDTO.fromModel(matricula));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMatricula(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (roles == null || (!roles.contains("ROLE_ADMIN"))) {
            throw new BusinessRuleException("Acceso denegado: Solo los administradores pueden eliminar matriculas.");
        }

        matriculaService.eliminarMatricula(id);
        return ResponseEntity.noContent().build();
    }
}
