package com.myapp.model;

public class Facultad {
    private double id;
    private String nombre;
    private Persona decano;

    public Facultad(double id, String nombre, Persona decano) {
        this.id = id;
        this.nombre = nombre;
        this.decano = decano;
    }

    @Override
    public String toString() {
        return "Facultad{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", decano=" + decano +
                '}';
    }
}
