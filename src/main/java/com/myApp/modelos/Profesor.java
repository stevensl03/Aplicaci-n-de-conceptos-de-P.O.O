package com.myApp.modelos;

public class Profesor extends Persona {
    private String tipoContrato;

    public Profesor(double id, String nombres, String apellidos, String email, String tipoContrato) {
        super(id, nombres, apellidos, email);
        this.tipoContrato = tipoContrato;
    }

    //getters y setters
    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    @Override
    public String toString() {
        return "Profesor{" +
                "tipoContrato='" + tipoContrato + '\'' +
                "} " + super.toString();
    }

}
