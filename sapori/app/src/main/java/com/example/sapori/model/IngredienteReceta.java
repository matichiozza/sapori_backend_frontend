package com.example.sapori.model;

import com.example.sapori.model.Ingrediente;

import java.io.Serializable;

public class IngredienteReceta implements Serializable {

    private Long id;
    private Ingrediente ingrediente;
    private double cantidad;
    private String unidad;

    // NUEVO
    private Long recetaId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    // NUEVO
    public Long getRecetaId() { return recetaId; }
    public void setRecetaId(Long recetaId) { this.recetaId = recetaId; }
}
