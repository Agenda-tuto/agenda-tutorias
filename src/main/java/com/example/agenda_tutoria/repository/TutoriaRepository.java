package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.Tutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface TutoriaRepository extends JpaRepository<Tutoria, String> {
    List<Tutoria> findByEstudianteId(String estudianteId);
    List<Tutoria> findByProfesorId(String profesorId);
    List<Tutoria> findByEstado(Tutoria.Estado estado);
    Page<Tutoria> findByEstudianteId(String estudianteId, Pageable pageable);
    Page<Tutoria> findByProfesorId(String profesorId, Pageable pageable);
    Page<Tutoria> findByEstado(Tutoria.Estado estado, Pageable pageable);
    long countByEstudianteId(String estudianteId);
    long countByProfesorIdAndEstado(String profesorId, Tutoria.Estado estado);
    List<Tutoria> findByFechaHoraBeforeAndEstadoIn(LocalDateTime fecha, List<Tutoria.Estado> estados);
}
