package com.example.agenda_tutoria.controller;

import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.model.Pago;
import com.example.agenda_tutoria.model.Tutoria;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.model.UsuarioPublicoDto;
import com.example.agenda_tutoria.repository.PagoRepository;
import com.example.agenda_tutoria.service.NotificacionService;
import com.example.agenda_tutoria.service.PagoService;
import com.example.agenda_tutoria.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TutoriaRepository tutoriaRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private NotificacionService notificacionService;
    @Autowired private TransaccionRepository transaccionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private PagoService pagoService;
    @Autowired private PasswordEncoder passwordEncoder;

    // ───────────────────────── DASHBOARD ─────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        List<Tutoria> misTutorias = tutoriaRepository.findByEstudianteId(usuario.getId());

        List<Tutoria> proximas = misTutorias.stream()
                .filter(t -> t.getEstado() == Tutoria.Estado.ACEPTADA
                        || t.getEstado() == Tutoria.Estado.PENDIENTE)
                .sorted(Comparator.comparing(Tutoria::getFechaHora,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(3)
                .collect(Collectors.toList());

        long aceptadas = misTutorias.stream()
                .filter(t -> t.getEstado() == Tutoria.Estado.ACEPTADA).count();
        long pendientes = misTutorias.stream()
                .filter(t -> t.getEstado() == Tutoria.Estado.PENDIENTE).count();

        // Excluir al usuario logueado de tutoresDestacados
        List<Usuario> tutoresDestacados = usuarioRepository.findByRol("PROFESOR")
                .stream()
                .filter(t -> !t.getId().equals(usuario.getId()))
                .limit(3)
                .collect(Collectors.toList());

        long noLeidas = notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());

        // Última tutoría (la más reciente por fecha)
        Tutoria ultimaTutoria = misTutorias.stream()
                .filter(t -> t.getFechaHora() != null)
                .max(Comparator.comparing(Tutoria::getFechaHora))
                .orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("misTutorias", misTutorias);
        model.addAttribute("ultimaTutoria", ultimaTutoria);
        model.addAttribute("proximasTutorias", proximas);
        model.addAttribute("tutoriasAceptadas", aceptadas);
        model.addAttribute("tutoriasPendientes", pendientes);
        model.addAttribute("tutoresDestacados", tutoresDestacados);
        model.addAttribute("noLeidas", noLeidas);
        return "estudiante/dashboard";
    }

    // ───────────────────────── MIS TUTORÍAS (paginated) ─────────────────────────
    @GetMapping("/tutorias")
    public String misTutorias(@RequestParam(defaultValue = "0") int page,
                              Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));

        List<Tutoria> todas = tutoriaRepository.findByEstudianteId(usuario.getId());
        todas.sort(Comparator.comparing(Tutoria::getFechaHora,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int pageSize = 10;
        int total = todas.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        int from = page * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Tutoria> pageTutorias = from >= total ? List.of() : todas.subList(from, to);

        model.addAttribute("tutorias", pageTutorias);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "estudiante/tutorias";
    }

    // ───────────────────────── TUTORES ─────────────────────────
    @GetMapping("/tutores")
    public String verTutores(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(defaultValue = "0") int page,
            Authentication auth, Model model) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        // Excluir al usuario logueado de la lista de tutores
        List<Usuario> todosTutores = usuarioRepository.findByRol("PROFESOR")
                .stream()
                .filter(t -> !t.getId().equals(usuario.getId()))
                .collect(Collectors.toList());

        if (q != null && !q.isBlank()) {
            String busq = q.toLowerCase();
            todosTutores = todosTutores.stream()
                    .filter(t -> (t.getNombre() != null && t.getNombre().toLowerCase().contains(busq))
                            || (t.getEspecialidad() != null && t.getEspecialidad().toLowerCase().contains(busq)))
                    .collect(Collectors.toList());
        }
        if (especialidad != null && !especialidad.isBlank()) {
            todosTutores = todosTutores.stream()
                    .filter(t -> especialidad.equalsIgnoreCase(t.getEspecialidad()))
                    .collect(Collectors.toList());
        }
        if (precioMax != null && precioMax > 0) {
            todosTutores = todosTutores.stream()
                    .filter(t -> t.getPrecioPorHora() == null || t.getPrecioPorHora() <= precioMax)
                    .collect(Collectors.toList());
        }

        int pageSize = 8;
        int totalTutores = todosTutores.size();
        int totalPages = (int) Math.ceil((double) totalTutores / pageSize);
        if (totalPages == 0) totalPages = 1;
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalTutores);
        List<Usuario> tutores = (fromIndex >= totalTutores) ? List.of()
                : todosTutores.subList(fromIndex, toIndex);

        // Dropdown de especialidades también excluye al propio usuario
        List<String> especialidades = usuarioRepository.findByRol("PROFESOR").stream()
                .filter(t -> !t.getId().equals(usuario.getId()))
                .map(Usuario::getEspecialidad)
                .filter(e -> e != null && !e.isBlank())
                .distinct().sorted().collect(Collectors.toList());

        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
        model.addAttribute("tutores", tutores);
        model.addAttribute("especialidades", especialidades);
        model.addAttribute("q", q);
        model.addAttribute("especialidadSeleccionada", especialidad);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "estudiante/tutores";
    }

    // ───────────────────────── PERFIL TUTOR (con DTO) ─────────────────────────
    @GetMapping("/tutor/{id}")
    public String verPerfilTutor(@PathVariable String id,
            Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        Usuario tutor = usuarioRepository.findById(id).orElseThrow();

        UsuarioPublicoDto tutorDto = UsuarioPublicoDto.fromUsuario(tutor);

        List<UsuarioPublicoDto> relacionados = usuarioRepository.findByRol("PROFESOR").stream()
                .filter(t -> !t.getId().equals(id))
                .filter(t -> t.getEspecialidad() != null
                        && tutor.getEspecialidad() != null
                        && t.getEspecialidad().equalsIgnoreCase(tutor.getEspecialidad()))
                .limit(3)
                .map(UsuarioPublicoDto::fromUsuario)
                .collect(Collectors.toList());

        if (relacionados.isEmpty()) {
            relacionados = usuarioRepository.findByRol("PROFESOR").stream()
                    .filter(t -> !t.getId().equals(id))
                    .limit(3)
                    .map(UsuarioPublicoDto::fromUsuario)
                    .collect(Collectors.toList());
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
        model.addAttribute("tutor", tutorDto);
        model.addAttribute("relacionados", relacionados);
        return "estudiante/tutor-perfil";
    }

    // ───────────────────────── AGENDAR ─────────────────────────
    @GetMapping("/agendar/{profesorId}")
    public String mostrarAgendar(@PathVariable String profesorId,
            Authentication auth, Model model) {
        Usuario estudiante = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        // Solo bloquear si es ESTUDIANTE sin verificar; un PROFESOR siempre puede operar
        boolean esProfesor = "PROFESOR".equals(estudiante.getRol());
        if (!esProfesor && Boolean.FALSE.equals(estudiante.getCuentaVerificada())) {
            return "redirect:/estudiante/dashboard?sinVerificar=true";
        }

        // Un profesor no puede agendarse a sí mismo
        if (estudiante.getId().equals(profesorId)) {
            return "redirect:/estudiante/tutores?error=propio";
        }

        Usuario profesor = usuarioRepository.findById(profesorId).orElseThrow();
        model.addAttribute("usuario", estudiante);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(estudiante.getId()));
        model.addAttribute("profesor", profesor);
        model.addAttribute("tutoria", new Tutoria());
        model.addAttribute("balanceEstudiante", estudiante.getBalance());
        return "estudiante/agendar";
    }

    @PostMapping("/agendar/{profesorId}")
    public String agendar(@PathVariable String profesorId,
            @ModelAttribute Tutoria tutoria,
            Authentication auth) {
        Usuario estudiante = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        Usuario profesor = usuarioRepository.findById(profesorId).orElseThrow();

        // Solo bloquear si es ESTUDIANTE sin verificar; un PROFESOR siempre puede operar
        boolean esProfesor = "PROFESOR".equals(estudiante.getRol());
        if (!esProfesor && Boolean.FALSE.equals(estudiante.getCuentaVerificada())) {
            return "redirect:/estudiante/dashboard?sinVerificar=true";
        }

        // Un profesor no puede agendarse a sí mismo
        if (estudiante.getId().equals(profesorId)) {
            return "redirect:/estudiante/tutores?error=propio";
        }

        // Validar fecha: mínimo 24 horas desde ahora
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime minFecha = ahora.plusHours(24);
        if (tutoria.getFechaHora() == null || tutoria.getFechaHora().isBefore(minFecha)) {
            return "redirect:/estudiante/agendar/" + profesorId + "?error=fechaMinima";
        }

        // Validar horario: solo entre 7:00 y 20:00
        int hora = tutoria.getFechaHora().getHour();
        if (hora < 7 || hora >= 20) {
            return "redirect:/estudiante/agendar/" + profesorId + "?error=horario";
        }

        // Auto-asignar materia del profesor (se asume al elegir al tutor)
        if (profesor.getMaterias() != null && !profesor.getMaterias().isEmpty()) {
            tutoria.setMateria(profesor.getMaterias().get(0));
        }

        double costo = pagoService.calcularCosto(
                profesor.getPrecioPorHora(), tutoria.getDuracionMin());

        if (!pagoService.tieneSaldo(estudiante, costo)) {
            return "redirect:/estudiante/agendar/" + profesorId + "?saldoInsuficiente=true";
        }

        tutoria.setEstudianteId(estudiante.getId());
        tutoria.setEstudianteNombre(estudiante.getNombre());
        tutoria.setProfesorId(profesorId);
        tutoria.setProfesorNombre(profesor.getNombre());
        tutoria.setEstado(Tutoria.Estado.PENDIENTE);
        tutoria.setPrecioTotal(costo);
        tutoriaRepository.save(tutoria);

        pagoService.procesarPago(estudiante, profesor, costo, tutoria.getId());

        notificacionService.crear(
                profesor.getId(),
                "Nueva solicitud de tutoría",
                estudiante.getNombre() + " quiere una tutoría de "
                        + tutoria.getMateria() + ". Pago procesado: $"
                        + String.format("%,.0f", costo),
                Notificacion.Tipo.TUTORIA_AGENDADA);

        return "redirect:/estudiante/dashboard?agendado=true";
    }

    // ───────────────────────── BILLETERA ─────────────────────────
    @GetMapping("/billetera")
    public String mostrarBilletera(@RequestParam(defaultValue = "0") int page,
                                   Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));

        Page<Pago> pagosPage = pagoRepository
                .findByUsuarioIdOrderByFechaDesc(usuario.getId(), PageRequest.of(page, 10));
        model.addAttribute("transacciones", pagosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagosPage.getTotalPages());
        return "estudiante/billetera";
    }

    @PostMapping("/billetera/recargar")
    public String recargar(@RequestParam Double monto, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        if (monto <= 0 || monto > 5000000) {
            return "redirect:/estudiante/billetera?error=monto";
        }

        pagoService.recargar(usuario, monto);
        return "redirect:/estudiante/billetera?recargado=true";
    }

    // ───────────────────────── SOLICITAR TUTOR ─────────────────────────
    @GetMapping("/solicitar-tutor")
    public String mostrarSolicitud(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
        return "estudiante/solicitar-tutor";
    }

    @PostMapping("/solicitar-tutor")
    public String solicitarTutor(@RequestParam String motivacion,
                                 @RequestParam(required = false) String materias,
                                 Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        usuario.setMotivacionTutor(motivacion);
        if (materias != null && !materias.isBlank()) {
            usuario.setMaterias(Arrays.asList(materias.split("\\s*,\\s*")));
        }
        usuario.setEstadoSolicitudTutor("PENDIENTE");
        usuarioRepository.save(usuario);
        return "redirect:/estudiante/dashboard?solicitud=true";
    }

    // ───────────────────────── CONFIRMAR TUTORÍA (EN_REVISION → PAGADA) ─────────────────────────
    @PostMapping("/tutorias/{id}/confirmar")
    public String confirmarTutoria(@PathVariable String id, Authentication auth) {
        Usuario estudiante = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (!t.getEstudianteId().equals(estudiante.getId())) return;
            if (t.getEstado() != Tutoria.Estado.EN_REVISION) return;

            String anterior = t.getEstado().name();
            t.setEstado(Tutoria.Estado.PAGADA);
            t.setPagado(true);
            tutoriaRepository.save(t);

            pagoService.registrarCambioEstado(t.getId(), anterior,
                    Tutoria.Estado.PAGADA.name(), estudiante.getId(), estudiante.getNombre(), "ESTUDIANTE");

            Pago ingreso = new Pago(t.getProfesorId(), t.getProfesorNombre(),
                    Pago.Tipo.INGRESO, t.getPrecioTotal() * 0.90,
                    "Pago por tutoría confirmada con " + t.getEstudianteNombre());
            pagoRepository.save(ingreso);

            usuarioRepository.findById(t.getProfesorId()).ifPresent(prof -> {
                double balance = prof.getBalance() != null ? prof.getBalance() : 0.0;
                prof.setBalance(balance + t.getPrecioTotal() * 0.90);
                usuarioRepository.save(prof);
            });

            notificacionService.crear(t.getProfesorId(),
                    "Pago confirmado por el estudiante",
                    t.getEstudianteNombre() + " confirmó la tutoría de " + t.getMateria()
                            + ". Pago de $" + String.format("%,.0f", t.getPrecioTotal() * 0.90) + " liberado.",
                    Notificacion.Tipo.TUTORIA_PAGADA);
            notificacionService.crear(t.getEstudianteId(),
                    "Tutoría confirmada",
                    "Has confirmado la tutoría de " + t.getMateria() + ". ¡Gracias!",
                    Notificacion.Tipo.TUTORIA_PAGADA);
        });

        return "redirect:/estudiante/dashboard?confirmada=true";
    }

    // ───────────────────────── ELIMINAR TUTORÍA ─────────────────────────
    @PostMapping("/tutorias/{id}/eliminar")
    public String eliminarTutoria(@PathVariable String id, Authentication auth) {
        Usuario estudiante = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        tutoriaRepository.findById(id).ifPresent(t -> {
            if (t.getEstudianteId().equals(estudiante.getId())
                    && (t.getEstado() == Tutoria.Estado.FINALIZADA
                        || t.getEstado() == Tutoria.Estado.PAGADA
                        || t.getEstado() == Tutoria.Estado.NO_ASISTIO
                        || t.getEstado() == Tutoria.Estado.EXPIRADA
                        || t.getEstado() == Tutoria.Estado.CANCELADA
                        || t.getEstado() == Tutoria.Estado.RECHAZADA)) {
                tutoriaRepository.delete(t);
            }
        });

        return "redirect:/estudiante/dashboard?eliminada=true";
    }

    // ───────────────────────── PERFIL ─────────────────────────
    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas",
                notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
        return "estudiante/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@RequestParam String nombre,
            @RequestParam(required = false) String passwordActual,
            @RequestParam(required = false) String passwordNueva,
            Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();
        usuario.setNombre(nombre);

        if (passwordActual != null && !passwordActual.isBlank()
                && passwordNueva != null && !passwordNueva.isBlank()) {
            if (passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                usuario.setPassword(passwordEncoder.encode(passwordNueva));
            } else {
                model.addAttribute("error", "La contraseña actual no es correcta.");
                model.addAttribute("usuario", usuario);
                model.addAttribute("noLeidas",
                        notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
                return "estudiante/perfil";
            }
        }

        usuarioRepository.save(usuario);
        return "redirect:/estudiante/perfil?ok=true";
    }
}