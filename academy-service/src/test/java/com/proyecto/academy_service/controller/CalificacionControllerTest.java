package com.proyecto.academy_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.academy_service.dto.CalificacionDTO;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Calificacion;
import com.proyecto.academy_service.service.CalificacionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalificacionController.class)
class CalificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CalificacionService calificacionService;

    @BeforeEach
    void setUpSecurity() {
        SecurityContextHolder.clearContext();
        var auth = new UsernamePasswordAuthenticationToken(
                "docente@colegioohiggins.cl",
                100L,
                List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    @Test
    @WithMockUser(roles = "DOCENTE")
    void calificar_comoDocente_retorna200() throws Exception {
        CalificacionDTO dto = CalificacionDTO.builder()
                .estudianteId(10L).asignaturaId(1L).nota(5.5).descripcion("Prueba").build();
        Calificacion calificacion = Calificacion.builder()
                .id(1L).estudianteId(10L).nota(5.5).descripcion("Prueba")
                .asignatura(Asignatura.builder().id(1L).build())
                .docenteId(100L)
                .build();

        when(calificacionService.registrarNota(any(Calificacion.class), anyLong())).thenReturn(calificacion);

        mockMvc.perform(post("/api/v1/calificaciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(5.5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void calificar_conRolAdmin_retorna200() throws Exception {
        CalificacionDTO dto = CalificacionDTO.builder()
                .estudianteId(10L).asignaturaId(1L).nota(5.5).descripcion("Prueba").build();
        Calificacion calificacion = Calificacion.builder()
                .id(1L).estudianteId(10L).nota(5.5).descripcion("Prueba")
                .asignatura(Asignatura.builder().id(1L).build())
                .docenteId(100L)
                .build();

        when(calificacionService.registrarNota(any(Calificacion.class), anyLong())).thenReturn(calificacion);

        mockMvc.perform(post("/api/v1/calificaciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void listarPorEstudiante_retorna200() throws Exception {
        Calificacion calificacion = Calificacion.builder()
                .id(1L).estudianteId(10L).nota(5.5)
                .asignatura(Asignatura.builder().id(1L).build())
                .build();
        when(calificacionService.listarNotasPorEstudiante(10L)).thenReturn(List.of(calificacion));

        mockMvc.perform(get("/api/v1/calificaciones/estudiante/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nota").value(5.5));
    }

    @Test
    @WithMockUser(roles = "DOCENTE")
    void actualizarCalificacion_comoDocente_retorna200() throws Exception {
        CalificacionDTO dto = CalificacionDTO.builder()
                .estudianteId(10L).asignaturaId(1L).nota(6.0).descripcion("Corregida").build();
        Calificacion calificacion = Calificacion.builder()
                .id(1L).estudianteId(10L).nota(6.0).descripcion("Corregida")
                .asignatura(Asignatura.builder().id(1L).build())
                .docenteId(100L)
                .build();

        when(calificacionService.actualizarCalificacion(anyLong(), any(Calificacion.class), anyLong()))
                .thenReturn(calificacion);

        mockMvc.perform(put("/api/v1/calificaciones/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(6.0));
    }
}
