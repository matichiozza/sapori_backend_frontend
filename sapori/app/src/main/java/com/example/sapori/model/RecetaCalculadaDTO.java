package com.example.sapori.model;

import com.example.sapori.IngredientesAdapter;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.fasterxml.jackson.annotation.JsonProperty;


public class RecetaCalculadaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    @JsonProperty("id")
    private Long id;

    @SerializedName("tipoCalculado")
    @JsonProperty("tipoCalculado")
    private Boolean tipoCalculado = Boolean.FALSE;

    @SerializedName("nombre")
    @JsonProperty("nombre")
    private String nombre;

    @SerializedName("porciones")
    @JsonProperty("porciones")
    private Integer porciones;

    @SerializedName("ingredientes")
    @JsonProperty("ingredientes")
    private List<IngredienteDTO> ingredientes;

    // --- Datos de la receta original ---

    @SerializedName("recetaOriginalId")
    @JsonProperty("recetaOriginalId")
    private Long recetaOriginalId;

    @SerializedName("nombreOriginal")
    @JsonProperty("nombreOriginal")
    private String nombreOriginal;

    @SerializedName("descripcionOriginal")
    @JsonProperty("descripcionOriginal")
    private String descripcionOriginal;

    @SerializedName("tipoOriginal")
    @JsonProperty("tipoOriginal")
    private String tipoOriginal;

    @SerializedName("porcionesOriginal")
    @JsonProperty("porcionesOriginal")
    private Integer porcionesOriginal;

    @SerializedName("fechaCreacionOriginal")
    @JsonProperty("fechaCreacionOriginal")
    private String fechaCreacionOriginal;

    @SerializedName("calificacionOriginal")
    @JsonProperty("calificacionOriginal")
    private String calificacionOriginal;

    @SerializedName("tiempoOriginal")
    @JsonProperty("tiempoOriginal")
    private Integer tiempoOriginal;

    @SerializedName("fotoOriginal")
    @JsonProperty("fotoOriginal")
    private String fotoOriginal;

    @SerializedName("autorOriginal")
    @JsonProperty("autorOriginal")
    private String autorOriginal;

    @SerializedName("estadoOriginal")
    @JsonProperty("estadoOriginal")
    private String estadoOriginal;

    @SerializedName("pasos")
    @JsonProperty("pasos")
    private List<String> pasos;

    @SerializedName("fechaCreacion")
    @JsonProperty("fechaCreacion")
    private String fechaCreacion;

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public Boolean getTipoCalculado() {
        return tipoCalculado;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getPorciones() {
        return porciones;
    }

    public List<IngredienteDTO> getIngredientes() {
        return ingredientes;
    }

    public Long getRecetaOriginalId() {
        return recetaOriginalId;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getDescripcionOriginal() {
        return descripcionOriginal;
    }

    public String getTipoOriginal() {
        return tipoOriginal;
    }

    public Integer getPorcionesOriginal() {
        return porcionesOriginal;
    }

    public String getFechaCreacionOriginal() {
        return fechaCreacionOriginal;
    }

    public String getCalificacionOriginal() {
        return calificacionOriginal;
    }

    public Integer getTiempoOriginal() {
        return tiempoOriginal;
    }

    public String getFotoOriginal() {
        return fotoOriginal;
    }

    public String getAutorOriginal() {
        return autorOriginal;
    }

    public String getEstadoOriginal() {
        return estadoOriginal;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    // --- Setters ---

    public void setTipoCalculado(Boolean tipoCalculado) {
        this.tipoCalculado = tipoCalculado;
    }

    public void setIngredientes(List<IngredienteDTO> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public void setPasos(List<String> pasos) {
        this.pasos = pasos;
    }

    public void setFotoOriginal(String fotoOriginal) {
        this.fotoOriginal = fotoOriginal;
    }

}