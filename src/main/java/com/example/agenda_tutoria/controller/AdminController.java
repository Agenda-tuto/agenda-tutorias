package com.example.agenda_tutoria.controller;

import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.model.SolicitudRetiro;
import com.example.agenda_tutoria.model.Tutoria;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.NotificacionRepository;
import com.example.agenda_tutoria.repository.SolicitudRetiroRepository;
import com.example.agenda_tutoria.repository.TutoriaRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import com.example.agenda_tutoria.service.NotificacionService;
import com.example.agenda_tutoria.service.PagoService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TutoriaRepository tutoriaRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private SolicitudRetiroRepository solicitudRetiroRepository;
    @Autowired private PagoService pagoService;

    // ───────────────────────── DASHBOARD ─────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalTutorias", tutoriaRepository.count());
        model.addAttribute("solicitudesPendientes",
                usuarioRepository.findByEstadoSolicitudTutor("PENDIENTE").size());
        return "admin/dashboard";
    }

    // ───────────────────────── USUARIOS ─────────────────────────
    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(defaultValue = "0") int page,
                                 Model model) {
        Page<Usuario> usuariosPage = usuarioRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("solicitudesPendientes",
                usuarioRepository.findByEstadoSolicitudTutor("PENDIENTE").size());
        return "admin/usuarios";
    }

    @Transactional
    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable String id, Authentication auth) {
        String adminCorreo = auth.getName();
        usuarioRepository.findById(id).ifPresent(u -> {
            if ("ADMIN".equals(u.getRol()) || u.getCorreo().equals(adminCorreo)) return;
            tutoriaRepository.deleteByEstudianteId(id);
            tutoriaRepository.deleteByProfesorId(id);
            notificacionRepository.deleteByUsuarioId(id);
            usuarioRepository.deleteById(id);
        });
        return "redirect:/admin/usuarios?eliminado=true";
    }

    // ───────────────────────── SOLICITUDES TUTOR ─────────────────────────
    @GetMapping("/solicitudes-tutor")
    public String solicitudesTutor(@RequestParam(defaultValue = "0") int page,
                                   Model model) {
        var todas = usuarioRepository.findByEstadoSolicitudTutor("PENDIENTE");
        int pageSize = 6;
        int totalPages = (int) Math.ceil((double) todas.size() / pageSize);
        if (totalPages == 0) totalPages = 1;
        int from = page * pageSize;
        int to = Math.min(from + pageSize, todas.size());
        model.addAttribute("solicitudes", from >= todas.size() ? List.of() : todas.subList(from, to));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("solicitudesPendientes", todas.size());
        return "admin/solicitudes-tutor";
    }

    @PostMapping("/solicitudes-tutor/{id}/aprobar")
public String aprobarSolicitud(@PathVariable String id) {
    usuarioRepository.findById(id).ifPresent(u -> {
        u.setRol("PROFESOR");
        u.setEstadoSolicitudTutor("APROBADA");
        u.setCuentaVerificada(true); // ← CORRECCIÓN: el tutor aprobado siempre puede operar
        usuarioRepository.save(u);
        notificacionService.crear(
                u.getId(),
                "¡Solicitud aprobada!",
                "Felicitaciones, ya eres tutor en Agenda Tutorías. Completa tu perfil.",
                Notificacion.Tipo.SOLICITUD_TUTOR_APROBADA);
    });
    return "redirect:/admin/solicitudes-tutor";
}

    @PostMapping("/solicitudes-tutor/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable String id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setEstadoSolicitudTutor("RECHAZADA");
            usuarioRepository.save(u);
            notificacionService.crear(
                    u.getId(),
                    "Solicitud de tutor rechazada",
                    "Tu solicitud para ser tutor no fue aprobada en esta ocasión.",
                    Notificacion.Tipo.SOLICITUD_TUTOR_RECHAZADA);
        });
        return "redirect:/admin/solicitudes-tutor";
    }

    // ───────────────────────── RETIROS ─────────────────────────
    @GetMapping("/retiros")
    public String verRetiros(@RequestParam(defaultValue = "0") int page,
                             Model model) {
        model.addAttribute("pendientes",
                solicitudRetiroRepository.findByEstado(SolicitudRetiro.Estado.PENDIENTE));
        Page<SolicitudRetiro> todosPage = solicitudRetiroRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("todos", todosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", todosPage.getTotalPages());
        model.addAttribute("solicitudesPendientes",
                usuarioRepository.findByEstadoSolicitudTutor("PENDIENTE").size());
        return "admin/retiros";
    }

    @PostMapping("/retiros/{id}/aprobar")
    public String aprobarRetiro(@PathVariable String id) {
        solicitudRetiroRepository.findById(id).ifPresent(r -> {
            r.setEstado(SolicitudRetiro.Estado.APROBADO);
            solicitudRetiroRepository.save(r);
            notificacionService.crear(
                    r.getTutorId(),
                    "Retiro aprobado",
                    "Tu solicitud de retiro por $" +
                            String.format("%,.0f", r.getMonto()) + " fue aprobada.",
                    Notificacion.Tipo.RETIRO_APROBADO);
        });
        return "redirect:/admin/retiros?ok=true";
    }

    @PostMapping("/retiros/{id}/rechazar")
    public String rechazarRetiro(@PathVariable String id) {
        solicitudRetiroRepository.findById(id).ifPresent(r -> {
            usuarioRepository.findById(r.getTutorId()).ifPresent(tutor -> {
                tutor.setBalance((tutor.getBalance() != null ? tutor.getBalance() : 0) + r.getMonto());
                usuarioRepository.save(tutor);
            });
            r.setEstado(SolicitudRetiro.Estado.RECHAZADO);
            solicitudRetiroRepository.save(r);
            notificacionService.crear(
                    r.getTutorId(),
                    "Retiro rechazado",
                    "Tu solicitud de retiro fue rechazada. El monto fue devuelto a tu balance.",
                    Notificacion.Tipo.RETIRO_RECHAZADO);
        });
        return "redirect:/admin/retiros?rechazado=true";
    }

    // ───────────────────────── GESTIÓN TUTORÍAS (estados nuevos) ─────────────────────────
    @GetMapping("/tutorias")
    public String listarTutorias(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String estado,
                                 Model model) {
        Page<Tutoria> tutoriasPage;
        if (estado != null && !estado.isBlank()) {
            try {
                tutoriasPage = tutoriaRepository.findByEstado(
                        Tutoria.Estado.valueOf(estado), PageRequest.of(page, 10));
            } catch (IllegalArgumentException e) {
                return "redirect:/admin/tutorias";
            }
        } else {
            tutoriasPage = tutoriaRepository.findAll(PageRequest.of(page, 10));
        }
        model.addAttribute("tutorias", tutoriasPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tutoriasPage.getTotalPages());
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("enRevisionCount",
                tutoriaRepository.findByEstado(Tutoria.Estado.EN_REVISION).size());
        model.addAttribute("solicitudesPendientes",
                usuarioRepository.findByEstadoSolicitudTutor("PENDIENTE").size());
        return "admin/tutorias";
    }

    @PostMapping("/tutorias/{id}/aprobar-pago")
    public String aprobarPago(@PathVariable String id, Authentication auth) {
        Usuario admin = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        tutoriaRepository.findById(id).ifPresent(t -> {
            if (t.getEstado() != Tutoria.Estado.EN_REVISION) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.PAGADA);
            t.setPagado(true);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(t.getId(), anterior,
                    Tutoria.Estado.PAGADA.name(), admin.getId(), admin.getNombre(), "ADMIN");

            notificacionService.crear(t.getEstudianteId(),
                    "Pago confirmado",
                    "El pago de tu tutoría de " + t.getMateria() + " ha sido liberado.",
                    Notificacion.Tipo.TUTORIA_PAGADA);
            notificacionService.crear(t.getProfesorId(),
                    "Pago recibido",
                    "El pago por la tutoría con " + t.getEstudianteNombre() + " ha sido confirmado.",
                    Notificacion.Tipo.TUTORIA_PAGADA);
        });
        return "redirect:/admin/tutorias?ok=pagada";
    }

    @PostMapping("/tutorias/{id}/no-asistio")
    public String marcarNoAsistio(@PathVariable String id, Authentication auth) {
        Usuario admin = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        tutoriaRepository.findById(id).ifPresent(t -> {
            if (t.getEstado() != Tutoria.Estado.EN_REVISION) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.NO_ASISTIO);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(t.getId(), anterior,
                    Tutoria.Estado.NO_ASISTIO.name(), admin.getId(), admin.getNombre(), "ADMIN");

            usuarioRepository.findById(t.getEstudianteId()).ifPresent(est -> {
                est.setBalance((est.getBalance() != null ? est.getBalance() : 0) + t.getPrecioTotal());
                usuarioRepository.save(est);
            });

            notificacionService.crear(t.getEstudianteId(),
                    "No asistió — reembolso",
                    "No asististe a la tutoría de " + t.getMateria() + ". El dinero fue devuelto.",
                    Notificacion.Tipo.TUTORIA_NO_ASISTIO);
            notificacionService.crear(t.getProfesorId(),
                    "Estudiante no asistió",
                    "El estudiante no asistió a la tutoría de " + t.getMateria() + ".",
                    Notificacion.Tipo.TUTORIA_NO_ASISTIO);
        });
        return "redirect:/admin/tutorias?ok=noasistio";
    }

    @PostMapping("/tutorias/{id}/expirada")
    public String marcarExpirada(@PathVariable String id, Authentication auth) {
        Usuario admin = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        tutoriaRepository.findById(id).ifPresent(t -> {
            if (t.getEstado() != Tutoria.Estado.EN_REVISION
                    && t.getEstado() != Tutoria.Estado.PENDIENTE
                    && t.getEstado() != Tutoria.Estado.ACEPTADA) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.EXPIRADA);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(t.getId(), anterior,
                    Tutoria.Estado.EXPIRADA.name(), admin.getId(), admin.getNombre(), "ADMIN");

            notificacionService.crear(t.getEstudianteId(),
                    "Tutoría expirada",
                    "La tutoría de " + t.getMateria() + " ha expirado.",
                    Notificacion.Tipo.TUTORIA_EXPIRADA);
        });
        return "redirect:/admin/tutorias?ok=expirada";
    }
}
