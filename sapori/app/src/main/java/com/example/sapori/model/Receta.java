package com.example.sapori.model;


import java.io.Serializable;
import java.util.List;

public class Receta implements Serializable {

    private Long id;

    private String nombre;
    private String descripcion;
    private String tipo;
    private Integer porciones;
    private String fechaCreacion; // ISO 8601 (ej: "2024-06-03T13:45:00")
    private Double calificacion;
    private Integer tiempo;
    private List<String> fotosPlato;
    private String estado; // "APROBADA", "PENDIENTE", etc.

    private Usuario autor;

    private List<IngredienteReceta> ingredientes;
    private List<PasoReceta> pasos;
    private List<ComentarioValoracion> comentarios;

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getPorciones() { return porciones; }
    public void setPorciones(Integer porciones) { this.porciones = porciones; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public Integer getTiempo() { return tiempo; }
    public void setTiempo(Integer tiempo) { this.tiempo = tiempo; }

    public List<String> getFotosPlato() { return fotosPlato; }
    public void setFotosPlato(List<String> fotosPlato) { this.fotosPlato = fotosPlato; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    public List<IngredienteReceta> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<IngredienteReceta> ingredientes) { this.ingredientes = ingredientes; }

    public List<PasoReceta> getPasos() { return pasos; }
    public void setPasos(List<PasoReceta> pasos) { this.pasos = pasos; }

    public List<ComentarioValoracion> getComentarios() { return comentarios; }
    public void setComentarios(List<ComentarioValoracion> comentarios) { this.comentarios = comentarios; }



}
