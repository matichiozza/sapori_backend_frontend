package com.example.demo.dto;

public class InscripcionRequest {
    private Long alumnoId;
    private Long cursoId;
    private Long cursoSedeId;

    // Constructor vacío
    public InscripcionRequest() {
    }

    // Constructor con parámetros
    public InscripcionRequest(Long alumnoId, Long cursoId, Long cursoSedeId) {
        this.alumnoId = alumnoId;
        this.cursoId = cursoId;
        this.cursoSedeId = cursoSedeId;
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

    public Long getCursoSedeId() {
        return cursoSedeId;
    }

    public void setCursoSedeId(Long cursoSedeId) {
        this.cursoSedeId = cursoSedeId;
    }
} 