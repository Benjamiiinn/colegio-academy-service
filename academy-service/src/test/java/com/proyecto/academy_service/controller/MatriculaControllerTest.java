package com.proyecto.academy_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.academy_service.dto.MatriculaDTO;
import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.model.Matricula;
import com.proyecto.academy_service.service.MatriculaService;

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

@WebMvcTest(MatriculaController.class)
class MatriculaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MatriculaService matriculaService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void matricular_comoAdmin_retorna200() throws Exception {
        MatriculaDTO dto = MatriculaDTO.builder().estudianteId(10L).cursoId(1L).build();
        Matricula matricula = Matricula.builder()
                .id(1L).estudianteId(10L)
                .curso(Curso.builder().id(1L).build())
                .build();

        when(matriculaService.matricularAlumno(any(Matricula.class))).thenReturn(matricula);

        mockMvc.perform(post("/api/v1/matriculas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value(10L));
    }

    @Test
    @WithMockUser
    void listarAlumnosPorCurso_retorna200() throws Exception {
        Matricula matricula = Matricula.builder()
                .id(1L).estudianteId(10L)
                .curso(Curso.builder().id(1L).build())
                .build();
        when(matriculaService.listarAlumnosPorCurso(1L)).thenReturn(List.of(matricula));

        mockMvc.perform(get("/api/v1/matriculas/curso/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estudianteId").value(10L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarMatricula_comoAdmin_retorna204() throws Exception {
        doNothing().when(matriculaService).eliminarMatricula(1L);

        mockMvc.perform(delete("/api/v1/matriculas/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
