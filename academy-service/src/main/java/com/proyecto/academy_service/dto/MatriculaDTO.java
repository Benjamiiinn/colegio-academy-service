package com.proyecto.academy_service.dto;

import com.proyecto.academy_service.model.Curso;
import com.proyecto.academy_service.model.Matricula;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatriculaDTO {
    private Long id;

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @NotNull(message = "El curso es obligatorio")
    private Long cursoId;

    public Matricula toModel() {
        return Matricula.builder()
                .id(this.id)
                .estudianteId(this.estudianteId)
                .curso(Curso.builder().id(this.cursoId).build())
                .build();
    }

    public static MatriculaDTO fromModel(Matricula m) {
        if (m == null) return null;
        return new MatriculaDTO(
            m.getId(), 
            m.getEstudianteId(), 
            m.getCurso() != null ? m.getCurso().getId() : null
        );
    }
}
