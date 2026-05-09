package com.proyecto.academy_service.dto;

import com.proyecto.academy_service.model.Asignatura;
import com.proyecto.academy_service.model.Curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AsignaturaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la asignatura es obligatorio")
    private String nombre;

    @NotNull(message = "El ID del docente es obligatorio")
    private Long idDocente;

    @NotNull(message = "El ID del curso es obligatorio")
    private Long cursoId;

    public Asignatura toModel() {
        Curso cursoRef = new Curso();
        cursoRef.setId(this.cursoId);
        return new Asignatura(id, nombre, idDocente, cursoRef);
    }

    public static AsignaturaDTO fromModel(Asignatura a) {
        if (a == null) return null;

        Long cId = (a.getCurso() != null) ? a.getCurso().getId() : null;
        return new AsignaturaDTO(a.getId(), a.getNombre(), a.getIdDocente(), cId);
    }
}
