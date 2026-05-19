package com.example.agenda_tutoria.controller;

import com.example.agenda_tutoria.model.Pago;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.PagoRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import com.example.agenda_tutoria.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
public class AuthController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private EmailService emailService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String verificado,
                        Model model) {
        if (error != null) model.addAttribute("error", "Correo o contraseña incorrectos.");
        if (logout != null) model.addAttribute("logout", "Sesión cerrada correctamente.");
        if (verificado != null) model.addAttribute("verificado",
            "¡Cuenta verificada! Ya puedes iniciar sesión y tienes $50.000 en tu billetera.");
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario, Model model) {
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo.");
            return "registro";
        }

        String codigo = String.format("%06d", new Random().nextInt(999999));

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol("ESTUDIANTE");
        usuario.setBalance(0.0);
        usuario.setCuentaVerificada(false);
        usuario.setCodigoVerificacion(codigo);
        usuarioRepository.save(usuario);

        // Enviar correo real
        emailService.enviarCodigoVerificacion(
            usuario.getCorreo(), usuario.getNombre(), codigo);

        return "redirect:/verificar?correo=" + usuario.getCorreo();
    }

    @GetMapping("/verificar")
    public String mostrarVerificacion(
            @RequestParam String correo,
            @RequestParam(required = false) String error,
            Model model) {
        model.addAttribute("correo", correo);
        if (error != null) model.addAttribute("error", "Código incorrecto. Inténtalo de nuevo.");
        return "verificar";
    }

    @PostMapping("/verificar")
    public String verificarCodigo(@RequestParam String correo,
                                  @RequestParam String codigo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) return "redirect:/login";

        if (!codigo.equals(usuario.getCodigoVerificacion())) {
            return "redirect:/verificar?correo=" + correo + "&error=true";
        }

        usuario.setCuentaVerificada(true);
        usuario.setCodigoVerificacion(null);
        usuario.setBalance(50000.0);
        usuarioRepository.save(usuario);

        Pago bono = new Pago(
            usuario.getId(), usuario.getNombre(),
            Pago.Tipo.RECARGA, 50000.0,
            "Bono de bienvenida al registrarte"
        );
        pagoRepository.save(bono);

        return "redirect:/login?verificado=true";
    }

    @GetMapping("/403")
    public String accesoDenegado() {
        return "403";
    }
}
