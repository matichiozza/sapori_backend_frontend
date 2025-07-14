package com.example.sapori.model;

public class IngredienteDTO {
    private String nombre;
    private String cantidad;
    private String unidad;
    private String imagenUrl;
    private String cantidadOriginal;

    public IngredienteDTO(String nombre, String cantidad, String unidad, String imagenUrl) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.imagenUrl = imagenUrl;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getCantidadOriginal() {
        return cantidadOriginal;
    }

    public void setCantidadOriginal(String cantidadOriginal) {
        this.cantidadOriginal = cantidadOriginal;
    }
}