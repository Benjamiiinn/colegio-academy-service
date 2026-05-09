package com.proyecto.academy_service.dto;

import com.proyecto.academy_service.model.Curso;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoDTO {

    private Long id;

    @NotBlank(message = "El nivel es obligatorio (ej: '1 Medio')")
    private String nivel;

    @NotBlank(message = "La letra es obligatoria (ej: 'A')")
    private String letra;

    public Curso toModel() {
        return Curso.builder()
                .id(this.id)
                .nivel(this.nivel)
                .letra(this.letra)
                .build();
    }

    public static CursoDTO fromModel(Curso c) {
        if (c == null) return null;
        return new CursoDTO(c.getId(), c.getNivel(), c.getLetra());
    }
}
