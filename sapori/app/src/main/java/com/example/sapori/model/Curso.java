package com.example.sapori.model;

import java.io.Serializable;
import java.util.List;

public class Curso implements Serializable {

    private Long id;

    private String nombre;
    private List<String> fotosUrl;
    private String modalidad; // Ej: "PRESENCIAL", "VIRTUAL", "HIBRIDA"
    private Float importe;
    private Integer duracion; // En semanas, meses, etc.
    private String objetivo;
    private String fechaInicio; // ISO 8601 (ej: "2024-06-03T13:45:00")
    private String fechaFin;
    private String estadoCurso; // "EN_CURSO", "FINALIZADO", "PROXIMAMENTE"
    private String descripcion;
    private String temario;
    private String requisitos;

    private List<PracticaCursoDTO> practicas;
    private List<Sede> sedes;
    private List<Alumno> inscriptos;
    private List<MaterialDeClase> materiales;

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<String> getFotosUrl() { return fotosUrl; }
    public void setFotosUrl(List<String> fotosUrl) { this.fotosUrl = fotosUrl; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public Float getImporte() { return importe; }
    public void setImporte(Float importe) { this.importe = importe; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getEstadoCurso() { return estadoCurso; }
    public void setEstadoCurso(String estadoCurso) { this.estadoCurso = estadoCurso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTemario() { return temario; }
    public void setTemario(String temario) { this.temario = temario; }

    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }

    public List<PracticaCursoDTO> getPracticas() { return practicas; }
    public void setPracticas(List<PracticaCursoDTO> practicas) { this.practicas = practicas; }

    public List<Sede> getSedes() { return sedes; }
    public void setSedes(List<Sede> sedes) { this.sedes = sedes; }

    public List<Alumno> getInscriptos() { return inscriptos; }
    public void setInscriptos(List<Alumno> inscriptos) { this.inscriptos = inscriptos; }

    public List<MaterialDeClase> getMateriales() { return materiales; }
    public void setMateriales(List<MaterialDeClase> materiales) { this.materiales = materiales; }

}
