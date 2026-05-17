package com.example.agenda_tutoria.security;

import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        String password = usuario.getPassword() != null
            ? usuario.getPassword() : "{noop}oauth2user";

        return new org.springframework.security.core.userdetails.User(
            usuario.getCorreo(),
            password,
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }
}