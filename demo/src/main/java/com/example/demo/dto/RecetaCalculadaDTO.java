package com.example.demo.dto;

import com.example.demo.modelo.Receta;
import com.example.demo.modelo.RecetaCalculada;

import java.util.List;
import java.util.stream.Collectors;

public class RecetaCalculadaDTO {
    private Long id;
    private String nombre;
    private Integer porciones;
    private String fechaCreacion;
    private Boolean tipoCalculado;

    // Datos receta original
    private Long recetaOriginalId;
    private String nombreOriginal;
    private String descripcionOriginal;
    private String tipoOriginal;
    private Integer porcionesOriginal;
    private String fechaCreacionOriginal;
    private String calificacionOriginal;
    private Integer tiempoOriginal;
    private String fotoOriginal;
    private String autorOriginal;
    private String estadoOriginal;

    private List<IngredienteDTO> ingredientes;
    private List<String> pasos;

    public RecetaCalculadaDTO(RecetaCalculada rc) {
        this.id = rc.getId();
        this.nombre = rc.getNombre();
        this.porciones = rc.getPorciones();
        this.fechaCreacion = rc.getFechaCreacion() != null ? rc.getFechaCreacion().toString() : "";
        this.tipoCalculado = rc.getTipoCalculado();

        Receta receta = rc.getRecetaOriginal();
        if (receta != null) {
            this.recetaOriginalId = receta.getId();
            this.nombreOriginal = receta.getNombre();
            this.descripcionOriginal = receta.getDescripcion();
            this.tipoOriginal = receta.getTipo();
            this.porcionesOriginal = receta.getPorciones();
            this.fechaCreacionOriginal = receta.getFechaCreacion() != null ? receta.getFechaCreacion().toString() : null;

            Double calificacion = receta.getCalificacionObj();
            this.calificacionOriginal = calificacion != null ? String.format("%.1f", calificacion) : "N/D";

            this.tiempoOriginal = receta.getTiempo();
            this.fotoOriginal = receta.getFotoPrincipal();
            this.autorOriginal = receta.getAutor() != null ? receta.getAutor().getAlias() : "Desconocido";
            this.estadoOriginal = receta.getEstado() != null ? receta.getEstado().name() : "Sin estado";

            this.ingredientes = receta.getIngredientes() != null
                    ? receta.getIngredientes().stream()
                    .map(ing -> new IngredienteDTO(
                            ing.getIngrediente().getNombre(),
                            String.valueOf(ing.getCantidad()),
                            ing.getUnidad(),
                            ing.getIngrediente().getImagenUrl() // Asegurate que exista este método
                    ))
                    .collect(Collectors.toList())
                    : null;

            this.pasos = receta.getPasos() != null
                    ? receta.getPasos().stream()
                    .map(paso -> paso.getDescripcion())
                    .collect(Collectors.toList())
                    : null;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getPorciones() {
        return porciones;
    }

    public void setPorciones(Integer porciones) {
        this.porciones = porciones;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getRecetaOriginalId() {
        return recetaOriginalId;
    }

    public void setRecetaOriginalId(Long recetaOriginalId) {
        this.recetaOriginalId = recetaOriginalId;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getDescripcionOriginal() {
        return descripcionOriginal;
    }

    public void setDescripcionOriginal(String descripcionOriginal) {
        this.descripcionOriginal = descripcionOriginal;
    }

    public String getTipoOriginal() {
        return tipoOriginal;
    }

    public void setTipoOriginal(String tipoOriginal) {
        this.tipoOriginal = tipoOriginal;
    }

    public Integer getPorcionesOriginal() {
        return porcionesOriginal;
    }

    public void setPorcionesOriginal(Integer porcionesOriginal) {
        this.porcionesOriginal = porcionesOriginal;
    }

    public String getFechaCreacionOriginal() {
        return fechaCreacionOriginal;
    }

    public void setFechaCreacionOriginal(String fechaCreacionOriginal) {
        this.fechaCreacionOriginal = fechaCreacionOriginal;
    }

    public String getCalificacionOriginal() {
        return calificacionOriginal;
    }

    public void setCalificacionOriginal(String calificacionOriginal) {
        this.calificacionOriginal = calificacionOriginal;
    }

    public Integer getTiempoOriginal() {
        return tiempoOriginal;
    }

    public void setTiempoOriginal(Integer tiempoOriginal) {
        this.tiempoOriginal = tiempoOriginal;
    }

    public String getFotoOriginal() {
        return fotoOriginal;
    }

    public void setFotoOriginal(String fotoOriginal) {
        this.fotoOriginal = fotoOriginal;
    }

    public String getAutorOriginal() {
        return autorOriginal;
    }

    public void setAutorOriginal(String autorOriginal) {
        this.autorOriginal = autorOriginal;
    }

    public String getEstadoOriginal() {
        return estadoOriginal;
    }

    public void setEstadoOriginal(String estadoOriginal) {
        this.estadoOriginal = estadoOriginal;
    }

    public List<IngredienteDTO> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<IngredienteDTO> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public void setPasos(List<String> pasos) {
        this.pasos = pasos;
    }
    public Boolean getTipoCalculado() {
        return tipoCalculado;
    }

    public void setTipoCalculado(Boolean tipoCalculado) {
        this.tipoCalculado = tipoCalculado;
    }
}