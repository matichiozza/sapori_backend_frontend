package com.example.sapori.model;

import java.io.Serializable;

public class PracticaCurso implements Serializable {

    private Long id;

    private String titulo;
    private String iconoTituloUrl;
    private String lista; // Puede venir como texto plano o en formato JSON

    private Curso curso; // Referencia al curso al que pertenece

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIconoTituloUrl() { return iconoTituloUrl; }
    public void setIconoTituloUrl(String iconoTituloUrl) { this.iconoTituloUrl = iconoTituloUrl; }

    public String getLista() { return lista; }
    public void setLista(String lista) { this.lista = lista; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
}