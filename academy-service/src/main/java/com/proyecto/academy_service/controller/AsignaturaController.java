package com.proyecto.academy_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.academy_service.dto.AsignaturaDTO;
import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.service.AsignaturaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/asignaturas")
@RequiredArgsConstructor
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    @PostMapping
    public ResponseEntity<AsignaturaDTO> crearAsignatura(
            @RequestHeader(value = "X-User-Role", required = false) String rolUsuario,
            @Valid @RequestBody AsignaturaDTO dto) {
        
        if (rolUsuario == null || (!rolUsuario.contains("ROLE_ADMIN"))) {
            throw new BusinessRuleException("Acceso denegado: Solo los administradores pueden crear asignaturas.");
        }
        
        Asignatura asignatura = asignaturaService.crearAsignatura(dto.toModel());
        return ResponseEntity.ok(AsignaturaDTO.fromModel(asignatura));
    }

    @GetMapping("/docente/{idDocente}")
    public ResponseEntity<List<AsignaturaDTO>> listarPorDocente(@PathVariable Long idDocente) {
        List<AsignaturaDTO> lista = asignaturaService.listarPorDocente(idDocente).stream()
                .map(AsignaturaDTO::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
    
}
