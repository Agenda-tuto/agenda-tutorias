package com.example.agenda_tutoria.controller;

import com.example.agenda_tutoria.model.Notificacion;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.NotificacionRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import com.example.agenda_tutoria.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public String verNotificaciones(@RequestParam(defaultValue = "0") int page,
                                    Authentication auth, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName()).orElseThrow();

        Page<Notificacion> notisPage = notificacionRepository
                .findByUsuarioIdOrderByFechaDesc(usuario.getId(), PageRequest.of(page, 10));

        model.addAttribute("notificaciones", notisPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notisPage.getTotalPages());
        model.addAttribute("noLeidas",
            notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
        notificacionService.marcarTodasLeidas(usuario.getId());
        return "notificaciones";
    }
}