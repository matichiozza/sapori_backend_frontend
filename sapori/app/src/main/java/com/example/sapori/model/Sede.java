package com.example.sapori.model;

import java.io.Serializable;
import java.util.List;

public class Sede implements Serializable {

    private Long id;

    private String nombre;
    private String direccion;
    private String telefono;

    private String zona;

    private List<String> fotosUrlSedes;

    private List<Curso> cursosDisponibles;

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public List<String> getFotosUrlSedes() { return fotosUrlSedes; }
    public void setFotosUrlSedes(List<String> fotosUrlSedes) { this.fotosUrlSedes = fotosUrlSedes; }

    public List<Curso> getCursosDisponibles() { return cursosDisponibles; }
    public void setCursosDisponibles(List<Curso> cursosDisponibles) { this.cursosDisponibles = cursosDisponibles; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}