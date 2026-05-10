package com.proyecto.academy_service.dto;

import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Calificacion;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionDTO {
    private Long id;

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    @NotNull(message = "El ID de la asignatura es obligatorio")
    private Long asignaturaId;

    @NotNull(message = "La nota es obligatoria")
    private Double nota;

    @NotNull(message = "La descripción es obligatoria")
    private String descripcion;

    public Calificacion toModel() {
        return Calificacion.builder()
                .id(this.id)
                .estudianteId(this.estudianteId)
                .nota(this.nota)
                .descripcion(this.descripcion)
                .asignatura(Asignatura.builder().id(this.asignaturaId).build())
                .build();
    }

    public static CalificacionDTO fromModel(Calificacion c) {
        if (c == null) return null;
        return new CalificacionDTO(
            c.getId(),
            c.getEstudianteId(),
            c.getAsignatura() != null ? c.getAsignatura().getId() : null,
            c.getNota(),
            c.getDescripcion()
        );
    }
}
