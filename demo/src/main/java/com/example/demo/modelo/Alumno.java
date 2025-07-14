package com.example.demo.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Se genera automáticamente en la DB
    @Column(name = "id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    private String nombreCompleto;
    private String numeroTramiteDNI;
    private String dniFrenteUrl;
    private String dniDorsoUrl;
    private String fechaCaducidad;
    private String numTarjetaCredito;
    private int codigoSeguridad;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "alumno_curso",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    @JsonIgnore
    private List<Curso> cursosInscriptos = new ArrayList<>();



    public List<Curso> getCursosInscriptos() {
        return cursosInscriptos;
    }

    public void setCursosInscriptos(List<Curso> cursosInscriptos) {
        this.cursosInscriptos = cursosInscriptos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNumeroTramiteDNI() {
        return numeroTramiteDNI;
    }

    public void setNumeroTramiteDNI(String numeroTramiteDNI) {
        this.numeroTramiteDNI = numeroTramiteDNI;
    }

    public String getDniFrenteUrl() {
        return dniFrenteUrl;
    }

    public void setDniFrenteUrl(String dniFrenteUrl) {
        this.dniFrenteUrl = dniFrenteUrl;
    }

    public String getDniDorsoUrl() {
        return dniDorsoUrl;
    }

    public void setDniDorsoUrl(String dniDorsoUrl) {
        this.dniDorsoUrl = dniDorsoUrl;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getNumTarjetaCredito() {
        return numTarjetaCredito;
    }

    public void setNumTarjetaCredito(String numTarjetaCredito) {
        this.numTarjetaCredito = numTarjetaCredito;
    }

    public int getCodigoSeguridad() {
        return codigoSeguridad;
    }

    public void setCodigoSeguridad(int codigoSeguridad) {
        this.codigoSeguridad = codigoSeguridad;
    }
}