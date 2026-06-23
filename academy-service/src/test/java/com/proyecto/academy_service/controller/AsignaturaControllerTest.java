package com.proyecto.academy_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.academy_service.dto.AsignaturaDTO;
import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.service.AsignaturaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AsignaturaController.class)
class AsignaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AsignaturaService asignaturaService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearAsignatura_comoAdmin_retorna200() throws Exception {
        AsignaturaDTO dto = AsignaturaDTO.builder().nombre("Matematicas").docenteId(100L).cursoId(1L).build();
        Asignatura asignatura = Asignatura.builder()
                .id(1L).nombre("Matematicas").docenteId(100L)
                .curso(Curso.builder().id(1L).build())
                .build();

        when(asignaturaService.crearAsignatura(any(Asignatura.class))).thenReturn(asignatura);

        mockMvc.perform(post("/api/v1/asignaturas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Matematicas"));
    }

    @Test
    @WithMockUser
    void listarTodas_retorna200() throws Exception {
        Asignatura asignatura = Asignatura.builder()
                .id(1L).nombre("Matematicas").docenteId(100L)
                .curso(Curso.builder().id(1L).build())
                .build();
        when(asignaturaService.listarTodas()).thenReturn(List.of(asignatura));

        mockMvc.perform(get("/api/v1/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Matematicas"));
    }

    @Test
    @WithMockUser
    void listarPorDocente_retorna200() throws Exception {
        Asignatura asignatura = Asignatura.builder()
                .id(1L).nombre("Matematicas").docenteId(100L)
                .curso(Curso.builder().id(1L).build())
                .build();
        when(asignaturaService.listarPorDocente(100L)).thenReturn(List.of(asignatura));

        mockMvc.perform(get("/api/v1/asignaturas/docente/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].docenteId").value(100L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarAsignatura_comoAdmin_retorna204() throws Exception {
        doNothing().when(asignaturaService).eliminarAsignatura(1L);

        mockMvc.perform(delete("/api/v1/asignaturas/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
