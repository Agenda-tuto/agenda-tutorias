package com.example.agenda_tutoria.service;

import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void crear(String usuarioId, String titulo, String mensaje, Notificacion.Tipo tipo) {
        notificacionRepository.save(new Notificacion(usuarioId, titulo, mensaje, tipo));
    }

    public void marcarTodasLeidas(String usuarioId) {
        Query query = new Query(Criteria.where("usuarioId").is(usuarioId).and("leida").is(false));
        Update update = new Update().set("leida", true);
        mongoTemplate.updateMulti(query, update, Notificacion.class);
    }
}