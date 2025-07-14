package com.example.demo.dto;

public class IngredienteDTO {
    private String nombre;
    private String cantidad;
    private String unidad;
    private String imagenUrl;
    private String cantidadOriginal; // 🔹 NUEVO

    public IngredienteDTO(String nombre, String cantidad, String unidad, String imagenUrl) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.imagenUrl = imagenUrl;
    }

    // Getters
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

    public String getCantidadOriginal() {
        return cantidadOriginal;
    }

    // Setters
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

    public void setCantidadOriginal(String cantidadOriginal) {
        this.cantidadOriginal = cantidadOriginal;
    }

    @Override
    public String toString() {
        return "IngredienteDTO{" +
                "nombre='" + nombre + '\'' +
                ", cantidad='" + cantidad + '\'' +
                ", unidad='" + unidad + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                ", cantidadOriginal='" + cantidadOriginal + '\'' +
                '}';
    }
}