package com.example.sapori.model;

import java.io.Serializable;

public class MaterialDeClase implements Serializable {

    private Long id;

    private String nombre;
    private String fecha; // ISO 8601 (ej: "2024-06-03T13:45:00")
    private int tamanio;  // en KB o MB según el backend
    private String archivoUrl;

    private Curso curso;

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getTamanio() { return tamanio; }
    public void setTamanio(int tamanio) { this.tamanio = tamanio; }

    public String getArchivoUrl() { return archivoUrl; }
    public void setArchivoUrl(String archivoUrl) { this.archivoUrl = archivoUrl; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
}