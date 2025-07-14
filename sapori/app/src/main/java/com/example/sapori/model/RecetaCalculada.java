package com.example.sapori.model;

import java.util.ArrayList;
import java.util.List;

public class RecetaCalculada {

    private Long id;
    private List<Ingrediente> ingredientesEscalados;
    private String nombre;
    private String autor;
    private String calificacion;
    private String fotoPrincipal; // Puede ser URL o base64
    private int porciones;
    private String tiempo;
    private String fecha;

    private Boolean tipoCalculado = Boolean.FALSE; // NUEVO campo

    public RecetaCalculada() {
        this.ingredientesEscalados = new ArrayList<>();
    }

    public RecetaCalculada(String nombre, String autor, String calificacion, String fotoPrincipal, int porciones, String tiempo, String fecha) {
        this.nombre = nombre;
        this.autor = autor;
        this.calificacion = calificacion;
        this.fotoPrincipal = fotoPrincipal;
        this.porciones = porciones;
        this.tiempo = tiempo;
        this.fecha = fecha;
    }

    // Getters y setters existentes...
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getCalificacion() { return calificacion; }
    public void setCalificacion(String calificacion) { this.calificacion = calificacion; }

    public String getFotoPrincipal() { return fotoPrincipal; }
    public void setFotoPrincipal(String fotoPrincipal) { this.fotoPrincipal = fotoPrincipal; }

    public int getPorciones() { return porciones; }
    public void setPorciones(int porciones) { this.porciones = porciones; }

    public String getTiempo() { return tiempo; }
    public void setTiempo(String tiempo) { this.tiempo = tiempo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<Ingrediente> getIngredientesEscalados() {
        if (ingredientesEscalados == null) {
            ingredientesEscalados = new ArrayList<>();
        }
        return ingredientesEscalados;
    }

    public void setIngredientesEscalados(List<Ingrediente> ingredientesEscalados) {
        this.ingredientesEscalados = ingredientesEscalados;
    }

    // 🔥 NUEVOS getters y setters para tipoCalculado
    public Boolean getTipoCalculado() {
        return tipoCalculado;
    }

    public void setTipoCalculado(Boolean tipoCalculado) {
        this.tipoCalculado = tipoCalculado;
    }
}
