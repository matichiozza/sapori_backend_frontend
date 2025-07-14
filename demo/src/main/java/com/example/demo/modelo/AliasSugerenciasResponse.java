package com.example.demo.modelo;

import java.util.List;

public class AliasSugerenciasResponse {

    private String mensaje;
    private List<String> sugerenciasAlias;

    // Constructor vacío
    public AliasSugerenciasResponse() {}

    // Getters y setters

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<String> getSugerenciasAlias() {
        return sugerenciasAlias;
    }

    public void setSugerenciasAlias(List<String> sugerenciasAlias) {
        this.sugerenciasAlias = sugerenciasAlias;
    }
}