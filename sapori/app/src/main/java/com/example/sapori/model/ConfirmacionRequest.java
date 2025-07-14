package com.example.sapori.model;

public class ConfirmacionRequest {
    private String email;
    private String codigo;

    public ConfirmacionRequest(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }

    public String getEmail() { return email; }
    public String getCodigo() { return codigo; }
}
