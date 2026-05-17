package com.example.agenda_tutoria.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notificaciones")
public class Notificacion {

    public enum Tipo {
        TUTORIA_AGENDADA, TUTORIA_ACEPTADA, TUTORIA_RECHAZADA,
        TUTORIA_EN_CURSO, TUTORIA_FINALIZADA, TUTORIA_EN_REVISION,
        TUTORIA_PAGADA, TUTORIA_NO_ASISTIO, TUTORIA_EXPIRADA,
        SOLICITUD_TUTOR_APROBADA, SOLICITUD_TUTOR_RECHAZADA,
        RETIRO_APROBADO, RETIRO_RECHAZADO
    }

    @Id
    private String id;

    private String usuarioId;      // quién recibe la notificación
    private String titulo;
    private String mensaje;
    private Tipo tipo;
    private boolean leida;
    private LocalDateTime fecha;

    public Notificacion() {
        this.leida = false;
        this.fecha = LocalDateTime.now();
    }

    public Notificacion(String usuarioId, String titulo, String mensaje, Tipo tipo) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.leida = false;
        this.fecha = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
