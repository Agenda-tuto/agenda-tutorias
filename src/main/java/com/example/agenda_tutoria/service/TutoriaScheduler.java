package com.example.agenda_tutoria.service;

import com.example.agenda_tutoria.model.HistorialEstado;
import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.model.Tutoria;
import com.example.agenda_tutoria.repository.HistorialEstadoRepository;
import com.example.agenda_tutoria.repository.TutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TutoriaScheduler {

    @Autowired private TutoriaRepository tutoriaRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private HistorialEstadoRepository historialEstadoRepository;

    @Scheduled(fixedRate = 3600000)
    public void expirarTutoriasVencidas() {
        List<Tutoria> vencidas = tutoriaRepository
                .findByFechaHoraBeforeAndEstadoIn(LocalDateTime.now(), List.of(
                        Tutoria.Estado.PENDIENTE,
                        Tutoria.Estado.ACEPTADA,
                        Tutoria.Estado.EN_CURSO,
                        Tutoria.Estado.REPROGRAMADA));

        for (Tutoria t : vencidas) {
            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.EXPIRADA);
            tutoriaRepository.save(t);

            HistorialEstado h = new HistorialEstado();
            h.setTutoriaId(t.getId());
            h.setEstadoAnterior(anterior);
            h.setEstadoNuevo(Tutoria.Estado.EXPIRADA.name());
            h.setCambiadoPorNombre("Sistema");
            h.setRol("SISTEMA");
            h.setFechaCambio(LocalDateTime.now());
            historialEstadoRepository.save(h);

            notificacionService.crear(t.getEstudianteId(),
                    "Tutoría expirada",
                    "La tutoría de " + t.getMateria() + " ha expirado automáticamente.",
                    Notificacion.Tipo.TUTORIA_EXPIRADA);

            if (t.getProfesorId() != null) {
                notificacionService.crear(t.getProfesorId(),
                        "Tutoría expirada",
                        "La tutoría de " + t.getMateria() + " con " + t.getEstudianteNombre()
                                + " ha expirado automáticamente.",
                        Notificacion.Tipo.TUTORIA_EXPIRADA);
            }
        }

        if (!vencidas.isEmpty()) {
            System.out.println("⏰ Auto-expiradas: " + vencidas.size() + " tutorías");
        }
    }
}
