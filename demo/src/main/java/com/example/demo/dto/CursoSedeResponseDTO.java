package com.example.demo.dto;

import com.example.demo.modelo.CursoSede;
import com.example.demo.modelo.Curso;
import com.example.demo.modelo.Sede;

public class CursoSedeResponseDTO {
    private Long id;
    private Long cursoId;
    private String cursoNombre;
    private Long sedeId;
    private String sedeNombre;
    private String cursadaHorarioDia;
    private int capacidad;
    private int vacantesDisponibles;
    private boolean hayDescuento;
    private int descuento;

    // Constructor vacío
    public CursoSedeResponseDTO() {}

    // Constructor que recibe un CursoSede
    public CursoSedeResponseDTO(CursoSede cursoSede) {
        this.id = cursoSede.getId();
        this.cursadaHorarioDia = cursoSede.getCursada_horario_dia();
        this.capacidad = cursoSede.getCapacidad();
        this.vacantesDisponibles = cursoSede.getVacantesDisponibles();
        this.hayDescuento = cursoSede.isHayDescuento();
        this.descuento = cursoSede.getDescuento();
        
        // Extraer información del curso
        if (cursoSede.getCurso() != null) {
            this.cursoId = cursoSede.getCurso().getId();
            this.cursoNombre = cursoSede.getCurso().getNombre();
        }
        
        // Extraer información de la sede
        if (cursoSede.getSede() != null) {
            this.sedeId = cursoSede.getSede().getId();
            this.sedeNombre = cursoSede.getSede().getNombre();
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getCursoNombre() {
        return cursoNombre;
    }

    public void setCursoNombre(String cursoNombre) {
        this.cursoNombre = cursoNombre;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public String getCursadaHorarioDia() {
        return cursadaHorarioDia;
    }

    public void setCursadaHorarioDia(String cursadaHorarioDia) {
        this.cursadaHorarioDia = cursadaHorarioDia;
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
} 