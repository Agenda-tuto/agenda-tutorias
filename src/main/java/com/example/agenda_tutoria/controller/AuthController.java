package com.example.agenda_tutoria.controller;

import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.model.SolicitudRetiro;
import com.example.agenda_tutoria.model.Transaccion;
import com.example.agenda_tutoria.model.Tutoria;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.NotificacionRepository;
import com.example.agenda_tutoria.repository.SolicitudRetiroRepository;
import com.example.agenda_tutoria.repository.TransaccionRepository;
import com.example.agenda_tutoria.repository.TutoriaRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import com.example.agenda_tutoria.service.NotificacionService;
import com.example.agenda_tutoria.service.PagoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profesor")
public class ProfesorController {

    @Autowired private TutoriaRepository tutoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private SolicitudRetiroRepository solicitudRetiroRepository;
    @Autowired private TransaccionRepository transaccionRepository;
    @Autowired private PagoService pagoService;

    // ───────────────────────── DASHBOARD ─────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            Authentication auth, Model model) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        if (!esProfesorOAdmin(profesor)) return "redirect:/403";

        Page<Tutoria> tutoriasPage = tutoriaRepository
                .findByProfesorId(profesor.getId(),
                        PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "fechaHora")));

        List<Tutoria> todas = tutoriaRepository.findByProfesorId(profesor.getId());
        long pendientes = todas.stream().filter(t -> t.getEstado() == Tutoria.Estado.PENDIENTE).count();
        long aceptadas = todas.stream().filter(t -> t.getEstado() == Tutoria.Estado.ACEPTADA).count();
        long noLeidas = notificacionRepository
                .countByUsuarioIdAndLeidaFalse(profesor.getId());

        model.addAttribute("profesor", profesor);
        model.addAttribute("tutorias", tutoriasPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tutoriasPage.getTotalPages());
        model.addAttribute("totalTutorias", todas.size());
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("aceptadas", aceptadas);
        model.addAttribute("noLeidas", noLeidas);
        return "profesor/dashboard";
    }

    // ───────────────────────── NOTIFICAR ─────────────────────────
    @PostMapping("/tutorias/{id}/notificar")
    public String notificar(@PathVariable String id,
            @RequestParam String mensaje,
            Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            notificacionService.crear(
                    t.getEstudianteId(),
                    "Mensaje de tu tutor",
                    profesor.getNombre() + ": " + mensaje,
                    Notificacion.Tipo.TUTORIA_AGENDADA);
        });

        return "redirect:/profesor/tutorias/" + id + "/gestionar?msg=enviado";
    }

    private boolean esProfesorOAdmin(Usuario usuario) {
        return "PROFESOR".equals(usuario.getRol()) || "ADMIN".equals(usuario.getRol());
    }

    // ───────────────────────── GESTIONAR TUTORÍA ─────────────────────────
    @GetMapping("/tutorias/{id}/gestionar")
    public String gestionarTutoria(@PathVariable String id,
            Model model, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        if (!esProfesorOAdmin(profesor)) return "redirect:/403";
        Tutoria t = tutoriaRepository.findById(id).orElse(null);

        if (t == null || !t.getProfesorId().equals(profesor.getId())) {
            return "redirect:/profesor/dashboard";
        }

        model.addAttribute("profesor", profesor);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(profesor.getId()));
        model.addAttribute("tutoria", t);
        return "profesor/gestionar-tutoria";
    }

    // ───────────────────────── ACEPTAR ─────────────────────────
    @PostMapping("/tutorias/{id}/aceptar")
    public String aceptar(@PathVariable String id,
            @RequestParam String mensaje,
            Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.PENDIENTE
                    && t.getEstado() != Tutoria.Estado.REPROGRAMADA) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.ACEPTADA);
            t.setMensajeTutor(mensaje);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.ACEPTADA.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(
                    t.getEstudianteId(),
                    "¡Tutoría aceptada!",
                    "Tu tutoría de " + t.getMateria() + " fue aceptada. Mensaje del tutor: " + mensaje,
                    Notificacion.Tipo.TUTORIA_ACEPTADA);
        });

        return "redirect:/profesor/dashboard?ok=aceptada";
    }

    // ───────────────────────── CANCELAR (con reembolso) ─────────────────────────
    @PostMapping("/tutorias/{id}/cancelar")
    public String cancelar(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.PENDIENTE
                    && t.getEstado() != Tutoria.Estado.ACEPTADA
                    && t.getEstado() != Tutoria.Estado.REPROGRAMADA) return;

            if (t.getPrecioTotal() != null && t.getPrecioTotal() > 0) {
                usuarioRepository.findById(t.getEstudianteId()).ifPresent(estudiante -> {
                    double balanceEst = estudiante.getBalance() != null ? estudiante.getBalance() : 0.0;
                    estudiante.setBalance(balanceEst + t.getPrecioTotal());
                    usuarioRepository.save(estudiante);

                    double gananciaRevertir = t.getPrecioTotal() * 0.90;
                    double balanceProf = profesor.getBalance() != null ? profesor.getBalance() : 0.0;
                    profesor.setBalance(Math.max(0, balanceProf - gananciaRevertir));
                    usuarioRepository.save(profesor);

                    Transaccion reembolso = new Transaccion(
                            estudiante.getId(), estudiante.getNombre(),
                            Transaccion.Tipo.RECARGA, t.getPrecioTotal(),
                            "Reembolso por cancelación de tutoría con " + profesor.getNombre());
                    reembolso.setReferenciaId(t.getId());
                    transaccionRepository.save(reembolso);
                });
            }

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.CANCELADA);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.CANCELADA.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(
                    t.getEstudianteId(),
                    "Tutoría cancelada — reembolso procesado",
                    "El profesor canceló la tutoría. El dinero fue devuelto a tu billetera.",
                    Notificacion.Tipo.TUTORIA_RECHAZADA);
        });

        return "redirect:/profesor/dashboard?ok=cancelada";
    }

    // ───────────────────────── RECHAZAR (con reembolso) ─────────────────────────
    @PostMapping("/tutorias/{id}/rechazar")
    public String rechazar(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.PENDIENTE) return;

            if (t.getPrecioTotal() != null && t.getPrecioTotal() > 0) {
                usuarioRepository.findById(t.getEstudianteId()).ifPresent(estudiante -> {
                    double balanceEst = estudiante.getBalance() != null ? estudiante.getBalance() : 0.0;
                    estudiante.setBalance(balanceEst + t.getPrecioTotal());
                    usuarioRepository.save(estudiante);

                    double gananciaRevertir = t.getPrecioTotal() * 0.90;
                    double balanceProf = profesor.getBalance() != null ? profesor.getBalance() : 0.0;
                    profesor.setBalance(Math.max(0, balanceProf - gananciaRevertir));
                    usuarioRepository.save(profesor);

                    Transaccion reembolso = new Transaccion(
                            estudiante.getId(), estudiante.getNombre(),
                            Transaccion.Tipo.RECARGA, t.getPrecioTotal(),
                            "Reembolso por rechazo de tutoría con " + profesor.getNombre());
                    reembolso.setReferenciaId(t.getId());
                    transaccionRepository.save(reembolso);
                });
            }

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.RECHAZADA);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.RECHAZADA.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(
                    t.getEstudianteId(),
                    "Tutoría rechazada — reembolso procesado",
                    "El tutor no pudo aceptar tu solicitud. El dinero fue devuelto a tu billetera.",
                    Notificacion.Tipo.TUTORIA_AGENDADA);
        });

        return "redirect:/profesor/dashboard?ok=rechazada";
    }

    // ───────────────────────── REPROGRAMAR ─────────────────────────
    @PostMapping("/tutorias/{id}/reprogramar")
    public String reprogramar(@PathVariable String id,
            @RequestParam String nuevaFecha,
            Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.PENDIENTE
                    && t.getEstado() != Tutoria.Estado.ACEPTADA) return;

            String anterior = t.getEstado().name();
            t.setFechaHora(java.time.LocalDateTime.parse(nuevaFecha));
            t.setEstado(Tutoria.Estado.REPROGRAMADA);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.REPROGRAMADA.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(
                    t.getEstudianteId(),
                    "Tutoría reprogramada",
                    "El profesor cambió la fecha de la tutoría.",
                    Notificacion.Tipo.TUTORIA_AGENDADA);
        });

        return "redirect:/profesor/dashboard?ok=reprogramada";
    }

    // ───────────────────────── FINALIZAR ─────────────────────────
    @PostMapping("/tutorias/{id}/finalizar")
    public String finalizar(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.ACEPTADA
                    && t.getEstado() != Tutoria.Estado.EN_CURSO) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.FINALIZADA);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.FINALIZADA.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(
                    t.getEstudianteId(),
                    "Tutoría finalizada",
                    "Tu tutoría ha sido marcada como finalizada.",
                    Notificacion.Tipo.TUTORIA_FINALIZADA);
        });

        return "redirect:/profesor/dashboard?ok=finalizada";
    }

    // ───────────────────────── INICIAR (EN_CURSO) ─────────────────────────
    @PostMapping("/tutorias/{id}/iniciar")
    public String iniciar(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.ACEPTADA) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.EN_CURSO);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.EN_CURSO.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(t.getEstudianteId(),
                    "Tutoría en curso",
                    "Tu tutoría con " + profesor.getNombre() + " ha comenzado.",
                    Notificacion.Tipo.TUTORIA_EN_CURSO);
        });
        return "redirect:/profesor/dashboard?ok=en_curso";
    }

    // ───────────────────────── EN REVISIÓN ─────────────────────────
    @PostMapping("/tutorias/{id}/en-revision")
    public String enRevision(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.FINALIZADA) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.EN_REVISION);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.EN_REVISION.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(t.getEstudianteId(),
                    "Tutoría en revisión",
                    "Tu tutoría está siendo revisada. Pronto se confirmará el pago.",
                    Notificacion.Tipo.TUTORIA_EN_REVISION);
        });
        return "redirect:/profesor/dashboard?ok=revision";
    }

    // ───────────────────────── REPORTAR AL ADMIN ─────────────────────────
    @PostMapping("/tutorias/{id}/reportar-admin")
    public String reportarAdmin(@PathVariable String id,
            @RequestParam String motivo,
            Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.EN_REVISION) return;

            // Buscar admin
            usuarioRepository.findByCorreo("admin@tutorias.com").ifPresent(admin -> {
                notificacionService.crear(admin.getId(),
                        "Profesor reporta tutoría sin confirmar",
                        "El profesor " + profesor.getNombre()
                                + " reporta que la tutoría de " + t.getMateria()
                                + " con " + t.getEstudianteNombre()
                                + " está en revisión y el estudiante no ha confirmado."
                                + " Motivo: " + motivo,
                        Notificacion.Tipo.TUTORIA_EN_REVISION);
            });

            notificacionService.crear(t.getProfesorId(),
                    "Reporte enviado al admin",
                    "Has reportado la tutoría de " + t.getMateria()
                            + ". El administrador revisará el caso.",
                    Notificacion.Tipo.TUTORIA_EN_REVISION);
        });

        return "redirect:/profesor/tutorias/" + id + "/gestionar?reportado=true";
    }

    // ───────────────────────── NO ASISTIÓ ─────────────────────────
    @PostMapping("/tutorias/{id}/no-asistio")
    public String noAsistio(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            if (t.getEstado() != Tutoria.Estado.ACEPTADA && t.getEstado() != Tutoria.Estado.EN_CURSO) return;

            // Reembolso completo al estudiante
            usuarioRepository.findById(t.getEstudianteId()).ifPresent(estudiante -> {
                double balanceEst = estudiante.getBalance() != null ? estudiante.getBalance() : 0.0;
                estudiante.setBalance(balanceEst + t.getPrecioTotal());
                usuarioRepository.save(estudiante);

                double gananciaRevertir = t.getPrecioTotal() * 0.90;
                double balanceProf = profesor.getBalance() != null ? profesor.getBalance() : 0.0;
                profesor.setBalance(Math.max(0, balanceProf - gananciaRevertir));
                usuarioRepository.save(profesor);

                Transaccion reembolso = new Transaccion(
                        estudiante.getId(), estudiante.getNombre(),
                        Transaccion.Tipo.RECARGA, t.getPrecioTotal(),
                        "Reembolso por inasistencia a tutoría con " + profesor.getNombre());
                reembolso.setReferenciaId(t.getId());
                transaccionRepository.save(reembolso);
            });

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.NO_ASISTIO);
            tutoriaRepository.save(t);
            pagoService.registrarCambioEstado(
                    t.getId(), anterior, Tutoria.Estado.NO_ASISTIO.name(),
                    profesor.getId(), profesor.getNombre(), "PROFESOR");

            notificacionService.crear(t.getEstudianteId(),
                    "No asististe a la tutoría",
                    "El tutor reportó que no asististe. El dinero fue devuelto a tu billetera.",
                    Notificacion.Tipo.TUTORIA_NO_ASISTIO);
        });
        return "redirect:/profesor/dashboard?ok=no_asistio";
    }

    // ───────────────────────── ELIMINAR ─────────────────────────
    @PostMapping("/tutorias/{id}/eliminar")
    public String eliminar(@PathVariable String id, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getProfesorId().equals(profesor.getId())) return;
            tutoriaRepository.delete(t);
        });

        return "redirect:/profesor/dashboard?ok=eliminada";
    }

    // ───────────────────────── PERFIL ─────────────────────────
    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        if (!esProfesorOAdmin(profesor)) return "redirect:/403";
        model.addAttribute("profesor", profesor);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(profesor.getId()));
        return "profesor/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute Usuario datos, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        profesor.setDescripcion(datos.getDescripcion());
        profesor.setEspecialidad(datos.getEspecialidad());
        profesor.setPrecioPorHora(datos.getPrecioPorHora());
        profesor.setAniosExperiencia(datos.getAniosExperiencia());
        usuarioRepository.save(profesor);
        return "redirect:/profesor/perfil?ok=true";
    }

    // ───────────────────────── RETIROS ─────────────────────────
    @GetMapping("/retiros")
    public String verRetiros(Authentication auth, Model model) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        if (!esProfesorOAdmin(profesor)) return "redirect:/403";
        model.addAttribute("profesor", profesor);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(profesor.getId()));
        model.addAttribute("retiros",
                solicitudRetiroRepository.findByTutorIdOrderByFechaDesc(profesor.getId()));
        model.addAttribute("transacciones",
                transaccionRepository.findByUsuarioIdOrderByFechaDesc(profesor.getId()));
        return "profesor/retiros";
    }

    @PostMapping("/retiros/solicitar")
    public String solicitarRetiro(@RequestParam Double monto, Authentication auth) {
        Usuario profesor = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        double balance = profesor.getBalance() != null ? profesor.getBalance() : 0.0;

        if (monto <= 0 || monto > balance) {
            return "redirect:/profesor/retiros?error=saldo";
        }

        profesor.setBalance(balance - monto);
        usuarioRepository.save(profesor);

        SolicitudRetiro solicitud = new SolicitudRetiro();
        solicitud.setTutorId(profesor.getId());
        solicitud.setTutorNombre(profesor.getNombre());
        solicitud.setMonto(monto);
        solicitudRetiroRepository.save(solicitud);

        Transaccion tx = new Transaccion(
                profesor.getId(), profesor.getNombre(),
                Transaccion.Tipo.RETIRO, monto,
                "Solicitud de retiro de fondos");
        transaccionRepository.save(tx);

        return "redirect:/profesor/retiros?ok=true";
    }
}
