package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, String> {
    List<HistorialEstado> findByTutoriaIdOrderByFechaCambioAsc(String tutoriaId);
    Page<HistorialEstado> findByTutoriaIdOrderByFechaCambioAsc(String tutoriaId, Pageable pageable);
}