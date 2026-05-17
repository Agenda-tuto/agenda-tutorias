package com.example.agenda_tutoria.repository;

import com.example.agenda_tutoria.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreo(String correo);
    List<Usuario> findByRol(String rol);
    List<Usuario> findByEstadoSolicitudTutor(String estado);
}
