package com.example.agenda_tutoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_estados")
public class HistorialEstado {

    @Id
    @Column(length = 36)
    private String id;

    private String tutoriaId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String cambiadoPorId;
    private String cambiadoPorNombre;
    private String rol;
    private LocalDateTime fechaCambio;
    private String observacion;

    public HistorialEstado() {
        this.id = UUID.randomUUID().toString();
        this.fechaCambio = LocalDateTime.now();
    }

    public HistorialEstado(String tutoriaId, String estadoAnterior, String estadoNuevo,
                           String cambiadoPorId, String cambiadoPorNombre, String rol) {
        this();
        this.tutoriaId = tutoriaId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.cambiadoPorId = cambiadoPorId;
        this.cambiadoPorNombre = cambiadoPorNombre;
        this.rol = rol;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutoriaId() { return tutoriaId; }
    public void setTutoriaId(String tutoriaId) { this.tutoriaId = tutoriaId; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getCambiadoPorId() { return cambiadoPorId; }
    public void setCambiadoPorId(String cambiadoPorId) { this.cambiadoPorId = cambiadoPorId; }

    public String getCambiadoPorNombre() { return cambiadoPorNombre; }
    public void setCambiadoPorNombre(String cambiadoPorNombre) { this.cambiadoPorNombre = cambiadoPorNombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}