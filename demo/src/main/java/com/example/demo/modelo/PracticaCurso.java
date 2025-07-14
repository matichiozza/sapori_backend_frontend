package com.example.demo.modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "practicas_curso")
public class PracticaCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(name = "icono_titulo_url")
    private String iconoTituloUrl;

    @Column(length = 4000)
    private String lista; // Por ejemplo: texto separado por saltos de línea o ítems JSON

    // 🔗 Relación con Curso
    @ManyToOne
    @JoinColumn(name = "curso_id")
    @JsonBackReference
    private Curso curso;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIconoTituloUrl() {
        return iconoTituloUrl;
    }

    public void setIconoTituloUrl(String iconoTituloUrl) {
        this.iconoTituloUrl = iconoTituloUrl;
    }

    public String getLista() {
        return lista;
    }

    public void setLista(String lista) {
        this.lista = lista;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}