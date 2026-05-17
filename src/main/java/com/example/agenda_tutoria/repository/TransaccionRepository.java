package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.Transaccion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TransaccionRepository extends MongoRepository<Transaccion, String> {
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(String usuarioId);
}
