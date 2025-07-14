package com.example.demo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "curso_sede")
public class CursoSede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    private String cursada_horario_dia;

    private int capacidad;

    @Column(name = "vacantes_disponibles")
    private Integer vacantesDisponibles = 0;  // ahora acepta null


    private boolean hayDescuento;

    private int descuento;

    public Long getId() {
        return id;
    }

    public String getCursada_horario_dia() {
        return cursada_horario_dia;
    }

    public void setCursada_horario_dia(String cursada_horario_dia) {
        this.cursada_horario_dia = cursada_horario_dia;
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

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getVacantesDisponibles() {
        return vacantesDisponibles;
    }

    public void setVacantesDisponibles(int vacantesDisponibles) {
        this.vacantesDisponibles = vacantesDisponibles;
    }

    public boolean isHayDescuento() {
        return hayDescuento;
    }

    public void setHayDescuento(boolean hayDescuento) {
        this.hayDescuento = hayDescuento;
    }

    public int getDescuento() {
        return descuento;
    }

    public void setDescuento(int descuento) {
        this.descuento = descuento;
    }

    public void actualizarVacantesDisponibles() {
        if (curso != null && curso.getInscriptos() != null) {
            this.vacantesDisponibles = this.capacidad - curso.getInscriptos().size();
        }
    }
}
