package com.proyecto.academy_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.academy_service.dto.CursoDTO;
import com.proyecto.academy_service.exception.BusinessRuleException;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.service.CursoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor    
public class CursoController {

    private final CursoService cursoService;

    @PostMapping
    public ResponseEntity<CursoDTO> crearCurso(
            @RequestHeader(value = "X-User-Role", required = false) String rolUsuario,
            @Valid @RequestBody CursoDTO dto) {
        
        if (rolUsuario == null ||  (!rolUsuario.contains("ROLE_ADMIN"))) {
            throw new BusinessRuleException("Acceso denegado: Solo los administradores pueden crear cursos.");
        }
        Curso curso = cursoService.crearCurso(dto.toModel());
        return ResponseEntity.ok().body(CursoDTO.fromModel(curso));
    }

    @GetMapping
    public ResponseEntity<List<CursoDTO>> listar() {
        List<CursoDTO> lista = cursoService.listarCursos().stream()
                .map(CursoDTO::fromModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(lista);
    }

}
