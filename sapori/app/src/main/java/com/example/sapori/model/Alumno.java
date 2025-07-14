package com.example.sapori.model;

public class Alumno {

    private Long id; // Mismo que el id del Usuario

    private String nombreCompleto;
    private String numeroTramiteDNI;
    private String dniFrenteUrl;
    private String dniDorsoUrl;
    private String fechaCaducidad;
    private String numTarjetaCredito;
    private int codigoSeguridad;

    private Usuario usuario;

    // Constructor vacÃ­o
    public Alumno() {
    }

    // Getters y Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}