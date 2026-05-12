package com.proyecto.academy_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.academy_service.model.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    boolean existsByNivelAndLetra(String nivel, String letra);
}
