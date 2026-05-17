package com.example.agenda_tutoria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class Pago {

    public enum Tipo {
        RECARGA, PAGO, COMISION, INGRESO, RETIRO
    }

    public enum Estado {
        COMPLETADA, PENDIENTE, RECHAZADA
    }

    @Id
    @Column(length = 36)
    private String id;

    private String usuarioId;
    private String usuarioNombre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Tipo tipo;

    private Double monto;
    private String descripcion;
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Estado estado;

    private String referenciaId;

    public Pago() {
        this.id = UUID.randomUUID().toString();
        this.fecha = LocalDateTime.now();
        this.estado = Estado.COMPLETADA;
    }

    public Pago(String usuarioId, String usuarioNombre, Tipo tipo, Double monto, String descripcion) {
        this();
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getReferenciaId() { return referenciaId; }
    public void setReferenciaId(String referenciaId) { this.referenciaId = referenciaId; }
}