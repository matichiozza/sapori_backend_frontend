package com.example.demo.dto;

public class BajaCursoRequest {
    private Long alumnoId;
    private Long cursoId;
    private String tipoReintegro; // "TARJETA" o "SALDO_FAVOR"
    private float montoReintegro;
    private int porcentajeReintegro;
    private Long cuentaCorrienteId;

    // Constructor vacío
    public BajaCursoRequest() {
    }

    // Constructor con parámetros
    public BajaCursoRequest(Long alumnoId, Long cursoId, String tipoReintegro, float montoReintegro, int porcentajeReintegro, Long cuentaCorrienteId) {
        this.alumnoId = alumnoId;
        this.cursoId = cursoId;
        this.tipoReintegro = tipoReintegro;
        this.montoReintegro = montoReintegro;
        this.porcentajeReintegro = porcentajeReintegro;
        this.cuentaCorrienteId = cuentaCorrienteId;
    }

    // Getters y Setters
    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getTipoReintegro() {
        return tipoReintegro;
    }

    public void setTipoReintegro(String tipoReintegro) {
        this.tipoReintegro = tipoReintegro;
    }

    public float getMontoReintegro() {
        return montoReintegro;
    }

    public void setMontoReintegro(float montoReintegro) {
        this.montoReintegro = montoReintegro;
    }

    public int getPorcentajeReintegro() {
        return porcentajeReintegro;
    }

    public void setPorcentajeReintegro(int porcentajeReintegro) {
        this.porcentajeReintegro = porcentajeReintegro;
    }

    public Long getCuentaCorrienteId() {
        return cuentaCorrienteId;
    }

    public void setCuentaCorrienteId(Long cuentaCorrienteId) {
        this.cuentaCorrienteId = cuentaCorrienteId;
    }
} 