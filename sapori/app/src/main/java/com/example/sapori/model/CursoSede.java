package com.example.sapori.model;

import java.io.Serializable;

public class CursoSede implements Serializable {
    private Long id;
    private Curso curso;
    private Sede sede;
    private String cursadaHorarioDia;
    private int capacidad;
    private Integer vacantesDisponibles;
    private boolean hayDescuento;
    private int descuento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public Sede getSede() { return sede; }
    public void setSede(Sede sede) { this.sede = sede; }

    public String getCursadaHorarioDia() { return cursadaHorarioDia; }
    public void setCursadaHorarioDia(String cursadaHorarioDia) { this.cursadaHorarioDia = cursadaHorarioDia; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public Integer getVacantesDisponibles() { return vacantesDisponibles; }
    public void setVacantesDisponibles(Integer vacantesDisponibles) { this.vacantesDisponibles = vacantesDisponibles; }

    public boolean isHayDescuento() { return hayDescuento; }
    public void setHayDescuento(boolean hayDescuento) { this.hayDescuento = hayDescuento; }

    public int getDescuento() { return descuento; }
    public void setDescuento(int descuento) { this.descuento = descuento; }
} 