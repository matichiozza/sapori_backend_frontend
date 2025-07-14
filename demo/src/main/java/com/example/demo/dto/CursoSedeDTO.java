package com.example.demo.dto;

public class CursoSedeDTO {
    private Long cursoId;
    private Long sedeId;
    private String cursadaHorarioDia;
    private int capacidad;
    private boolean hayDescuento;
    private int descuento;

    // Constructores
    public CursoSedeDTO() {}

    public CursoSedeDTO(Long cursoId, Long sedeId, String cursadaHorarioDia, int capacidad, boolean hayDescuento, int descuento) {
        this.cursoId = cursoId;
        this.sedeId = sedeId;
        this.cursadaHorarioDia = cursadaHorarioDia;
        this.capacidad = capacidad;
        this.hayDescuento = hayDescuento;
        this.descuento = descuento;
    }

    // Getters y Setters
    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
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