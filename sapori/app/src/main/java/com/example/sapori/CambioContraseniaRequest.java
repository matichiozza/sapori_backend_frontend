package com.example.sapori;

public class CambioContraseniaRequest {
    private String email;
    private String nuevaContrasenia;

    public CambioContraseniaRequest(String email, String nuevaContrasenia) {
        this.email = email;
        this.nuevaContrasenia = nuevaContrasenia;
    }

    public String getEmail() {
        return email;
    }

    public String getNuevaContrasenia() {
        return nuevaContrasenia;
    }
}