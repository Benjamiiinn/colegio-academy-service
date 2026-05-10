package com.proyecto.academy_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.academy_service.model.Asignatura;

@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {
    List<Asignatura> findByCursoId(Long cursoId);
    List<Asignatura> findByDocenteId(Long docenteId);
}
