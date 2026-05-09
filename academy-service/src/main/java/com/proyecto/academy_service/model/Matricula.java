package com.proyecto.academy_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matriculas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idAlumno;
    
    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;
}
