package com.example.sapori.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Pago implements Serializable {

    private Long id;
    private Curso curso;
    private Sede sede;
    private EstadoPago estado;
    private float importe;
    private LocalDateTime fechaInicioCurso;

    public enum EstadoPago {
        A_PAGAR, PAGO, REEMBOLSADO
    }

    // Constructor vacío
    public Pago() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public float getImporte() {
        return importe;
    }

    public void setImporte(float importe) {
        this.importe = importe;
    }

    public LocalDateTime getFechaInicioCurso() {
        return fechaInicioCurso;
    }

    public void setFechaInicioCurso(LocalDateTime fechaInicioCurso) {
        this.fechaInicioCurso = fechaInicioCurso;
    }
} 