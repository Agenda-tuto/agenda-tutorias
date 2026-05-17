package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, String> {
    List<Pago> findByUsuarioIdOrderByFechaDesc(String usuarioId);
    Page<Pago> findByUsuarioIdOrderByFechaDesc(String usuarioId, Pageable pageable);
}