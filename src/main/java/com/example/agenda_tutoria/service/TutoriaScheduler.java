package com.example.agenda_tutoria.service;

import com.example.agenda_tutoria.model.HistorialEstado;
import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.model.Transaccion;
import com.example.agenda_tutoria.model.Tutoria;
import com.example.agenda_tutoria.repository.HistorialEstadoRepository;
import com.example.agenda_tutoria.repository.TransaccionRepository;
import com.example.agenda_tutoria.repository.TutoriaRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TutoriaScheduler {

    @Autowired private TutoriaRepository tutoriaRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private HistorialEstadoRepository historialEstadoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TransaccionRepository transaccionRepository;

    @Scheduled(fixedRate = 3600000)
    public void expirarTutoriasVencidas() {
        List<Tutoria> vencidas = tutoriaRepository
                .findByFechaHoraBeforeAndEstadoIn(LocalDateTime.now().minusHours(2), List.of(
                        Tutoria.Estado.PENDIENTE,
                        Tutoria.Estado.ACEPTADA,
                        Tutoria.Estado.REPROGRAMADA));

        List<Tutoria> enCurso = tutoriaRepository
                .findByFechaHoraBeforeAndEstadoIn(LocalDateTime.now().minusHours(24), List.of(
                        Tutoria.Estado.EN_CURSO));

        int total = 0;
        for (Tutoria t : vencidas) { expirarYReembolsar(t); total++; }
        for (Tutoria t : enCurso)  { expirarYReembolsar(t); total++; }

        if (total > 0) {
            System.out.println("⏰ Auto-expiradas con reembolso: " + total + " tutorías");
        }
    }

    private void expirarYReembolsar(Tutoria t) {
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

        if (t.getPrecioTotal() != null && t.getPrecioTotal() > 0) {
            usuarioRepository.findById(t.getEstudianteId()).ifPresent(est -> {
                double balanceEst = est.getBalance() != null ? est.getBalance() : 0.0;
                est.setBalance(balanceEst + t.getPrecioTotal());
                usuarioRepository.save(est);

                Transaccion reembolso = new Transaccion(
                        est.getId(), est.getNombre(),
                        Transaccion.Tipo.RECARGA, t.getPrecioTotal(),
                        "Reembolso por expiración de tutoría con " + t.getProfesorNombre());
                reembolso.setReferenciaId(t.getId());
                transaccionRepository.save(reembolso);
            });

            usuarioRepository.findById(t.getProfesorId()).ifPresent(prof -> {
                double ganancia = t.getPrecioTotal() * 0.90;
                double balanceProf = prof.getBalance() != null ? prof.getBalance() : 0.0;
                prof.setBalance(Math.max(0, balanceProf - ganancia));
                usuarioRepository.save(prof);
            });
        }

        notificacionService.crear(t.getEstudianteId(),
                "Tutoría expirada — reembolso procesado",
                "La tutoría de " + t.getMateria()
                    + " expiró. El dinero fue devuelto a tu billetera.",
                Notificacion.Tipo.TUTORIA_EXPIRADA);

        if (t.getProfesorId() != null) {
            notificacionService.crear(t.getProfesorId(),
                    "Tutoría expirada",
                    "La tutoría de " + t.getMateria() + " con " + t.getEstudianteNombre()
                            + " expiró. El pago fue revertido.",
                    Notificacion.Tipo.TUTORIA_EXPIRADA);
        }
    }
}