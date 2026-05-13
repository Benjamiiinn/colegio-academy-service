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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.academy_service.dto.CalificacionDTO;
import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.model.Calificacion;
import com.proyecto.academy_service.service.CalificacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;

    @PostMapping
    public ResponseEntity<CalificacionDTO> calificar(@Valid @RequestBody CalificacionDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (roles == null || (!roles.contains("ROLE_DOCENTE"))) {
            throw new BusinessRuleException("Acceso denegado: Solo los docentes pueden registrar calificaciones.");
        }

        Calificacion calificacion = calificacionService.registrarNota(dto.toModel());
        return ResponseEntity.ok(CalificacionDTO.fromModel(calificacion));
    }

    //Ver las calificaciones de un estudiante
    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<CalificacionDTO>> listarPorEstudiante(@PathVariable Long estudianteId) {
        List<CalificacionDTO> lista = calificacionService.listarNotasPorEstudiante(estudianteId).stream()
                .map(CalificacionDTO::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping
    public ResponseEntity<List<CalificacionDTO>> listarTodas() {
        List<CalificacionDTO> lista = calificacionService.listarTodas().stream()
                .map(CalificacionDTO::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}
