package com.example.agenda_tutoria.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.List;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nombre;

    @Indexed(unique = true)
    private String correo;

    private String password;

    private String rol; // ESTUDIANTE, PROFESOR, ADMIN
    // Billetera
    private Double balance;

    // Verificación de cuenta
    private Boolean cuentaVerificada;
    private String codigoVerificacion;

    // Documentos del tutor
    private String cvSimulado;
    private List<String> certificados;
    private String estadoDocumentos; // PENDIENTE, APROBADO, RECHAZADO

    // Perfil del tutor
    private String descripcion;
    private String fotoPerfil;
    private Double precioPorHora;
    private Double calificacionPromedio;
    private Integer totalCalificaciones;
    private String especialidad;
    private Integer aniosExperiencia;
    private List<String> materias;
    

    // Para solicitud de ser tutor
    private String estadoSolicitudTutor; // PENDIENTE, APROBADA, RECHAZADA
    private String motivacionTutor;

    public Usuario() {
        this.calificacionPromedio = 0.0;
        this.totalCalificaciones = 0;
        this.balance = 0.0;
        this.cuentaVerificada = false;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Double getPrecioPorHora() {
        return precioPorHora;
    }

    public void setPrecioPorHora(Double precioPorHora) {
        this.precioPorHora = precioPorHora;
    }

    public Double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(Double cal) {
        this.calificacionPromedio = cal;
    }

    public Integer getTotalCalificaciones() {
        return totalCalificaciones;
    }

    public void setTotalCalificaciones(Integer total) {
        this.totalCalificaciones = total;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer anios) {
        this.aniosExperiencia = anios;
    }

    public List<String> getMaterias() {
        return materias;
    }

    public void setMaterias(List<String> materias) {
        this.materias = materias;
    }

    public String getEstadoSolicitudTutor() {
        return estadoSolicitudTutor;
    }

    public void setEstadoSolicitudTutor(String estado) {
        this.estadoSolicitudTutor = estado;
    }

    public String getMotivacionTutor() {
        return motivacionTutor;
    }

    public void setMotivacionTutor(String motivacion) {
        this.motivacionTutor = motivacion;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Boolean getCuentaVerificada() {
        return cuentaVerificada;
    }

    public void setCuentaVerificada(Boolean cuentaVerificada) {
        this.cuentaVerificada = cuentaVerificada;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public String getCvSimulado() {
        return cvSimulado;
    }

    public void setCvSimulado(String cvSimulado) {
        this.cvSimulado = cvSimulado;
    }

    public List<String> getCertificados() {
        return certificados;
    }

    public void setCertificados(List<String> certificados) {
        this.certificados = certificados;
    }

    public String getEstadoDocumentos() {
        return estadoDocumentos;
    }

    public void setEstadoDocumentos(String estadoDocumentos) {
        this.estadoDocumentos = estadoDocumentos;
    }
}