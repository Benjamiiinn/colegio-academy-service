package com.proyecto.academy_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.academy_service.model.Matricula;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    List<Matricula> findByCursoId(Long cursoId);
    boolean existsByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
}
