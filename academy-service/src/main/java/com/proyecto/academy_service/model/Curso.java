package com.proyecto.academy_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nivel;
    private String letra;

    @OneToMany(mappedBy = "curso")
    private List<Asignatura> asignaturas;

    @OneToMany(mappedBy = "curso")
    private List<Matricula> matriculas;
}
