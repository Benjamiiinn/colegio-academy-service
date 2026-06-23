package com.proyecto.academy_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.academy_service.dto.CursoDTO;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.service.CursoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CursoController.class)
class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CursoService cursoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCurso_comoAdmin_retorna200() throws Exception {
        CursoDTO dto = CursoDTO.builder().nivel("1 Medio").letra("A").build();
        Curso curso = Curso.builder().id(1L).nivel("1 Medio").letra("A").build();

        when(cursoService.crearCurso(any(Curso.class))).thenReturn(curso);

        mockMvc.perform(post("/api/v1/cursos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value("1 Medio"))
                .andExpect(jsonPath("$.letra").value("A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listarCursos_retorna200() throws Exception {
        Curso curso = Curso.builder().id(1L).nivel("1 Medio").letra("A").build();
        when(cursoService.listarCursos()).thenReturn(List.of(curso));

        mockMvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nivel").value("1 Medio"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarCurso_comoAdmin_retorna200() throws Exception {
        CursoDTO dto = CursoDTO.builder().nivel("2 Medio").letra("B").build();
        Curso curso = Curso.builder().id(1L).nivel("2 Medio").letra("B").build();

        when(cursoService.actualizarCurso(any(Long.class), any(Curso.class))).thenReturn(curso);

        mockMvc.perform(put("/api/v1/cursos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value("2 Medio"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarCurso_comoAdmin_retorna204() throws Exception {
        doNothing().when(cursoService).eliminarCurso(1L);

        mockMvc.perform(delete("/api/v1/cursos/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
