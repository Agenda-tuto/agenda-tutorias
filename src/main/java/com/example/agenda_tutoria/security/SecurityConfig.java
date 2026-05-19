package com.example.agenda_tutoria.security;

import com.example.agenda_tutoria.model.Transaccion;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.TransaccionRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return request -> {
            OAuth2User oauth2User = delegate.loadUser(request);

            String correo = oauth2User.getAttribute("email");
            String nombre = oauth2User.getAttribute("name");

            // Buscar o crear usuario en MongoDB
            Usuario usuario = usuarioRepository.findByCorreo(correo)
                    .orElseGet(() -> {
                        Usuario nuevo = new Usuario();
                        nuevo.setNombre(nombre);
                        nuevo.setCorreo(correo);
                        nuevo.setPassword(passwordEncoder().encode(UUID.randomUUID().toString()));
                        nuevo.setRol("ESTUDIANTE");
                        nuevo.setBalance(50000.0);
                        nuevo.setCuentaVerificada(true);
                        usuarioRepository.save(nuevo);

                        transaccionRepository.save(new Transaccion(
                                nuevo.getId(), nuevo.getNombre(),
                                Transaccion.Tipo.RECARGA, 50000.0,
                                "Bono de bienvenida — registro con Google"));

                        return nuevo;
                    });

            // Devolver OAuth2User con el rol correcto de MongoDB
            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())),
                    oauth2User.getAttributes(),
                    "email");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/registro", "/verificar",
                                "/reenviar-codigo",
                                "/css/**", "/js/**", "/img/**",
                                "/oauth2/**", "/login/oauth2/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/profesor/**").authenticated()
                        .requestMatchers("/estudiante/**").hasAnyRole("ESTUDIANTE", "PROFESOR")
                        .requestMatchers("/notificaciones/**").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            String rol = authentication.getAuthorities()
                                    .iterator().next().getAuthority();
                            if (rol.equals("ROLE_ADMIN"))
                                response.sendRedirect("/admin/dashboard");
                            else if (rol.equals("ROLE_PROFESOR"))
                                response.sendRedirect("/profesor/dashboard");
                            else
                                response.sendRedirect("/estudiante/dashboard");
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService()))
                        .successHandler((request, response, authentication) -> {
                            String rol = authentication.getAuthorities()
                                    .iterator().next().getAuthority();
                            if (rol.equals("ROLE_ADMIN"))
                                response.sendRedirect("/admin/dashboard");
                            else if (rol.equals("ROLE_PROFESOR"))
                                response.sendRedirect("/profesor/dashboard");
                            else
                                response.sendRedirect("/estudiante/dashboard");
                        }))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login")
                        .maximumSessions(1))
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403"));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }
}