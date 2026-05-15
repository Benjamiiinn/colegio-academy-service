package com.proyecto.academy_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.academy_service.model.Calificacion;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByEstudianteId(Long estudianteId);
    List<Calificacion> findByDocenteId(Long docenteId);
}
