package com.example.demo.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sedes")
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String direccion;

    private String telefono;

    private String zona;
    // URLs de fotos de la sede
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sede_fotos", joinColumns = @JoinColumn(name = "sede_id"))
    @Column(name = "url_foto")
    private List<String> fotosUrlSedes = new ArrayList<>();

    // 🔁 Cursos que se dictan en esta sede
    @ManyToMany(mappedBy = "sedes", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Curso> cursosDisponibles = new ArrayList<>();

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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<String> getFotosUrlSedes() {
        return fotosUrlSedes;
    }

    public void setFotosUrlSedes(List<String> fotosUrlSedes) {
        this.fotosUrlSedes = fotosUrlSedes;
    }

    public List<Curso> getCursosDisponibles() {
        return cursosDisponibles;
    }


    public void setCursosDisponibles(List<Curso> cursosDisponibles) {
        this.cursosDisponibles = cursosDisponibles;
    }


    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}