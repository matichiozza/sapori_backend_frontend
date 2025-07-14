package com.example.sapori.model;

import java.io.Serializable;

// DTO para PracticaCurso, sin referencia al curso para evitar ciclos JSON
public class PracticaCursoDTO implements Serializable {
    private Long id;
    private String titulo;
    private String iconoTituloUrl;
    private String lista;

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIconoTituloUrl() { return iconoTituloUrl; }
    public void setIconoTituloUrl(String iconoTituloUrl) { this.iconoTituloUrl = iconoTituloUrl; }

    public String getLista() { return lista; }
    public void setLista(String lista) { this.lista = lista; }
}
