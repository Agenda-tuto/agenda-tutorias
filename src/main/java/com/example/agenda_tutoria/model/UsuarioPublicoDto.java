package com.example.agenda_tutoria.model;

/**
 * DTO que expone solo los campos públicos de un tutor.
 * Evita filtrar password, codigoVerificacion, cvSimulado y certificados.
 */
public class UsuarioPublicoDto {

    private String id;
    private String nombre;
    private String correo;
    private String descripcion;
    private String fotoPerfil;
    private Double precioPorHora;
    private Double calificacionPromedio;
    private Integer totalCalificaciones;
    private String especialidad;
    private Integer aniosExperiencia;
    private java.util.List<String> materias;
    private String rol;

    public UsuarioPublicoDto() {
    }

    /** Construye el DTO a partir de un Usuario completo */
    public static UsuarioPublicoDto fromUsuario(Usuario u) {
        UsuarioPublicoDto dto = new UsuarioPublicoDto();
        dto.id = u.getId();
        dto.nombre = u.getNombre();
        dto.correo = u.getCorreo();
        dto.descripcion = u.getDescripcion();
        dto.fotoPerfil = u.getFotoPerfil();
        dto.precioPorHora = u.getPrecioPorHora();
        dto.calificacionPromedio = u.getCalificacionPromedio();
        dto.totalCalificaciones = u.getTotalCalificaciones();
        dto.especialidad = u.getEspecialidad();
        dto.aniosExperiencia = u.getAniosExperiencia();
        dto.materias = u.getMaterias();
        dto.rol = u.getRol();
        return dto;
    }

    // ─── Getters ───
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public Double getPrecioPorHora() {
        return precioPorHora;
    }

    public Double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public Integer getTotalCalificaciones() {
        return totalCalificaciones;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public java.util.List<String> getMaterias() {
        return materias;
    }

    public String getRol() {
        return rol;
    }
}
