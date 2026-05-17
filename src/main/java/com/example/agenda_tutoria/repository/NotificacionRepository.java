package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(String usuarioId);
    Page<Notificacion> findByUsuarioIdOrderByFechaDesc(String usuarioId, Pageable pageable);
    List<Notificacion> findByUsuarioIdAndLeidaFalse(String usuarioId);
    long countByUsuarioIdAndLeidaFalse(String usuarioId);
}