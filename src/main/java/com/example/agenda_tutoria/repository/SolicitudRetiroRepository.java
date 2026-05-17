package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.SolicitudRetiro;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SolicitudRetiroRepository extends MongoRepository<SolicitudRetiro, String> {
    List<SolicitudRetiro> findByTutorIdOrderByFechaDesc(String tutorId);
    List<SolicitudRetiro> findByEstado(SolicitudRetiro.Estado estado);
}