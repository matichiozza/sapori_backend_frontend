package com.example.sapori.model;

import java.time.LocalDateTime;

public class Asistencia {
    private Long id;
    private int clase;
    private String fecha; // Usar String para compatibilidad con Gson
    private String asistencia;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getClase() { return clase; }
    public void setClase(int clase) { this.clase = clase; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getAsistencia() { return asistencia; }
    public void setAsistencia(String asistencia) { this.asistencia = asistencia; }
} 